package me.padi.jxh.core.common

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.SHA512
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import dev.whyoleg.cryptography.algorithms.RSA as InternalRSA

object RSA {
    @OptIn(DelicateCryptographyApi::class)
    fun encrypt(plaintext: String, exponent: String, modulus: String): String {

        val cipher = CryptographyProvider.Default.get(InternalRSA.PKCS1)

        val pubKey = cipher.publicKeyDecoder(SHA512).decodeFromByteArrayBlocking(
            format = InternalRSA.PublicKey.Format.DER.Generic,
            bytes = (exponent to modulus).asPublicDer()
        )

        return Base64.encode(pubKey.encryptor().encryptBlocking(plaintext.encodeToByteArray()))
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun encrypt(plaintext: String, x509: String): String {
        val (exponentB64, modulusB64, modulusBytesLen) = parseSpkiToExponentModulusBase64(x509)

        val maxBlock = modulusBytesLen - 11
        require(maxBlock > 0) { "Invalid RSA key size derived from X.509 public key" }

        val plainBytes = plaintext.encodeToByteArray()
        require(plainBytes.size <= maxBlock) { "Plaintext too long for single-block RSA with PKCS1Padding: ${plainBytes.size} > $maxBlock" }

        return encrypt(plaintext, exponentB64, modulusB64)
    }
}

private fun Pair<String,String>.asPublicDer(): ByteArray {

    val exponent = Base64.decode(first)
    val modulus  = Base64.decode(second)

    fun Buffer.writeDerLength(len: Int) {
        when {
            len < 0x80 -> writeByte(len.toByte())
            len <= 0xFF -> {
                writeByte(0x81.toByte())
                writeByte(len.toByte())
            }
            else -> {
                writeByte(0x82.toByte())
                writeByte((len shr 8).toByte())
                writeByte(len.toByte())
            }
        }
    }

    fun derInteger(bytes: ByteArray): ByteArray {
        val out = Buffer()

        // Ensure positive INTEGER (leading 0 if high bit = 1)
        val v = if (bytes.first().toInt() and 0x80 != 0) byteArrayOf(0x00) + bytes else bytes

        out.writeByte(0x02)           // INTEGER
        out.writeDerLength(v.size)
        out.write(v)

        return out.readByteArray()
    }

    fun derSequence(vararg parts: ByteArray): ByteArray {
        val body = Buffer()
        parts.forEach { body.write(it) }
        val bodyBytes = body.readByteArray()

        val out = Buffer()
        out.writeByte(0x30)           // SEQUENCE
        out.writeDerLength(bodyBytes.size)
        out.write(bodyBytes)

        return out.readByteArray()
    }

    // AlgorithmIdentifier: rsaEncryption OID + NULL
    val algorithmId = byteArrayOf(
        0x30, 0x0D,
        0x06, 0x09,
        0x2A, 0x86.toByte(), 0x48, 0x86.toByte(),
        0xF7.toByte(), 0x0D, 0x01, 0x01, 0x01, // 1.2.840.113549.1.1.1
        0x05, 0x00
    )

    val rsaPubKey = derSequence(
        derInteger(modulus),
        derInteger(exponent)
    )

    // BIT STRING (0 unused bits + RSAPublicKey)
    val bitStringBuf = Buffer()
    bitStringBuf.writeByte(0x03)  // BIT STRING
    bitStringBuf.writeDerLength(1 + rsaPubKey.size)
    bitStringBuf.writeByte(0x00)  // unused bits
    bitStringBuf.write(rsaPubKey)

    return derSequence(algorithmId, bitStringBuf.readByteArray())
}

@OptIn(ExperimentalEncodingApi::class)
private fun parseSpkiToExponentModulusBase64(spkiBase64: String): Triple<String, String, Int> {
    val spkiDer = Base64.decode(spkiBase64)
    val spkiSeq = derExpect(0x30, spkiDer, 0).second

    var offset = 0
    // AlgorithmIdentifier (ignore content)
    val algSeqTLV = derExpect(0x30, spkiSeq, offset)
    offset += algSeqTLV.third

    // subjectPublicKey BIT STRING
    val bitStringTLV = derExpect(0x03, spkiSeq, offset)
    // BIT STRING value: first byte = number of unused bits, remaining is the RSAPublicKey DER
    val bitStringValue = bitStringTLV.second
    require(bitStringValue.isNotEmpty()) { "Invalid BIT STRING" }
    val rsaPubDer = bitStringValue.copyOfRange(1, bitStringValue.size)

    // RSAPublicKey ::= SEQUENCE { modulus INTEGER, publicExponent INTEGER }
    val rsaSeq = derExpect(0x30, rsaPubDer, 0).second
    var roff = 0
    val modulusBytes = derExpect(0x02, rsaSeq, roff).second.let { asn1IntegerToUnsigned(it) }
    roff += derExpect(0x02, rsaSeq, roff).third
    val exponentBytes = derExpect(0x02, rsaSeq, roff).second.let { asn1IntegerToUnsigned(it) }

    val modulusB64 = Base64.encode(modulusBytes)
    val exponentB64 = Base64.encode(exponentBytes)
    return Triple(exponentB64, modulusB64, modulusBytes.size)
}

private fun asn1IntegerToUnsigned(bytes: ByteArray): ByteArray {
    if (bytes.isEmpty()) return bytes
    var start = 0
    while (start < bytes.size - 1 && bytes[start] == 0.toByte()) {
        start++
    }
    return bytes.copyOfRange(start, bytes.size)
}

// Return: Triple(tag, valueBytes, totalTlvLen)
private fun derExpect(expectedTag: Int, data: ByteArray, start: Int): Triple<Int, ByteArray, Int> {
    require(start < data.size) { "DER out of bounds" }
    val tag = data[start].toInt() and 0xFF
    require(tag == expectedTag) { "Unexpected DER tag: ${tag.toString(16)} expected ${expectedTag.toString(16)}" }
    val (len, lenBytes) = derReadLength(data, start + 1)
    val valueStart = start + 1 + lenBytes
    val valueEnd = valueStart + len
    require(valueEnd <= data.size) { "DER length exceeds input" }
    val value = data.copyOfRange(valueStart, valueEnd)
    return Triple(tag, value, (valueEnd - start))
}

// Return: Pair(length, bytesConsumedForLength)
private fun derReadLength(data: ByteArray, pos: Int): Pair<Int, Int> {
    val first = data[pos].toInt() and 0xFF
    if (first and 0x80 == 0) {
        return first to 1
    }
    val numBytes = first and 0x7F
    require(numBytes in 1..4) { "Unsupported DER length bytes: $numBytes" }
    var len = 0
    for (i in 0 until numBytes) {
        len = (len shl 8) or (data[pos + 1 + i].toInt() and 0xFF)
    }
    return len to (1 + numBytes)
}

private fun splitStringByUtf8ByteLimit(text: String, maxBytes: Int): List<String> {
    if (text.isEmpty()) return emptyList()
    require(maxBytes > 0) { "maxBytes must be > 0" }
    val result = mutableListOf<String>()
    var i = 0
    while (i < text.length) {
        var j = i
        var lastGood = i
        while (j <= text.length) {
            val candidate = text.substring(i, j)
            val size = candidate.encodeToByteArray().size
            if (size <= maxBytes) {
                lastGood = j
                j++
            } else {
                break
            }
        }
        if (lastGood == i) {
            // 单个字符已超过限制（理论上不会发生在合理的 RSA 块大小下）
            // 强行切分为防御措施
            val ch = text[i].toString()
            result.add(ch)
            i++
        } else {
            result.add(text.substring(i, lastGood))
            i = lastGood
        }
    }
    return result
}
