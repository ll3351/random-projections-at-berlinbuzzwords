package com.stefansavev.core.serialization

import java.io.{InputStream, OutputStream}
import java.nio.ByteBuffer

//TODO: Investigate if efficient code can be generated by using Scala specializations

/**
 * Serialization of ints using java ByteBuffer
 */
object ByteSerializer {

  val bufferSize = 1

  def internalBuffer(): Array[Byte] = {
    Array.ofDim[Byte](bufferSize)
  }

  def toByteArray(value: Byte, bytes: Array[Byte]): Array[Byte] = {
    ByteBuffer.wrap(bytes).put(value)
    bytes
  }

  def toByte(bytes: Array[Byte]): Byte = {
    return ByteBuffer.wrap(bytes).get()
  }

  def write(outputStream: OutputStream, value: Byte, bytes: Array[Byte] = internalBuffer()): Unit = {
    outputStream.write(toByteArray(value, bytes))
  }

  def read(inputStream: InputStream, bytes: Array[Byte] = internalBuffer()): Byte = {
    inputStream.read(bytes)
    toByte(bytes)
  }

  def sizeInBytes: Long = {
    bufferSize
  }
}

/**
 * Serialization of ints using java ByteBuffer
 */
object IntSerializer {
  val bufferSize = 4

  def internalBuffer(): Array[Byte] = {
    Array.ofDim[Byte](bufferSize)
  }

  def toByteArray(value: Int, bytes: Array[Byte]): Array[Byte] = {
    ByteBuffer.wrap(bytes).putInt(value)
    bytes
  }

  def toInt(bytes: Array[Byte]): Int = {
    return ByteBuffer.wrap(bytes).getInt()
  }

  def write(outputStream: OutputStream, value: Int, bytes: Array[Byte] = internalBuffer()): Unit = {
    outputStream.write(toByteArray(value, bytes))
  }

  def read(inputStream: InputStream, bytes: Array[Byte] = internalBuffer()): Int = {
    inputStream.read(bytes)
    toInt(bytes)
  }

  def sizeInBytes: Long = {
    bufferSize
  }
}

/**
 * Serialization of shorts using java ByteBuffer
 */
object ShortSerializer {

  val bufferSize = 2

  def internalBuffer(): Array[Byte] = {
    Array.ofDim[Byte](bufferSize)
  }

  def toByteArray(value: Short, bytes: Array[Byte]): Array[Byte] = {
    ByteBuffer.wrap(bytes).putShort(value)
    bytes
  }

  def toShort(bytes: Array[Byte]): Short = {
    return ByteBuffer.wrap(bytes).getShort()
  }

  def write(outputStream: OutputStream, value: Short, bytes: Array[Byte] = internalBuffer()): Unit = {
    outputStream.write(toByteArray(value, bytes))
  }

  def read(inputStream: InputStream, bytes: Array[Byte] = internalBuffer()): Short = {
    inputStream.read(bytes)
    toShort(bytes)
  }

  def sizeInBytes: Long = {
    bufferSize
  }
}

/**
 * Serialization of floats using java ByteBuffer
 */
object FloatSerializer {
  val bufferSize = 4

  def internalBuffer(): Array[Byte] = {
    Array.ofDim[Byte](bufferSize)
  }

  def toByteArray(value: Float, bytes: Array[Byte]): Array[Byte] = {
    ByteBuffer.wrap(bytes).putFloat(value)
    bytes
  }

  def toShort(bytes: Array[Byte]): Float = {
    return ByteBuffer.wrap(bytes).getFloat()
  }

  def write(outputStream: OutputStream, value: Float, bytes: Array[Byte] = internalBuffer()): Unit = {
    outputStream.write(toByteArray(value, bytes))
  }

  def read(inputStream: InputStream, bytes: Array[Byte] = internalBuffer()): Float = {
    inputStream.read(bytes)
    toShort(bytes)
  }

  def sizeInBytes: Long = {
    bufferSize
  }
}

/**
 * Serialization of strings using java ByteBuffer
 */
object StringSerializer {

  def write(outputStream: OutputStream, value: String): Unit = {
    IntSerializer.write(outputStream, value.length)
    val buffer = ByteBuffer.allocate(2 * value.length)
    var i = 0
    while (i < value.length) {
      buffer.putChar(value(i))
      i += 1
    }
    buffer.rewind()
    outputStream.write(buffer.array())
  }

  def read(inputStream: InputStream): String = {
    val len = IntSerializer.read(inputStream)
    val buffer = Array.ofDim[Byte](len*2)
    inputStream.read(buffer)
    val byteBuffer = ByteBuffer.wrap(buffer)
    val output = Array.ofDim[Char](len)
    var i = 0
    while (i < len) {
      output(i) = byteBuffer.getChar()
      i += 1
    }
    new String(output)
  }

  def sizeInBytes(input: String): Long = {
    IntSerializer.sizeInBytes + 2 * input.length
  }
}

/**
 * Serialization of doubles using java ByteBuffer
 */
object DoubleSerializer {

  val bufferSize = 8

  def internalBuffer(): Array[Byte] = {
    Array.ofDim[Byte](bufferSize)
  }

  def toByteArray(value: Double, bytes: Array[Byte]): Array[Byte] = {
    val bytes = Array.ofDim[Byte](8)
    ByteBuffer.wrap(bytes).putDouble(value)
    bytes
  }

  def toDouble(bytes: Array[Byte]): Double = {
    return ByteBuffer.wrap(bytes).getDouble()
  }

  def write(outputStream: OutputStream, value: Double, bytes: Array[Byte] = internalBuffer()): Unit = {
    outputStream.write(toByteArray(value, bytes))
  }

  def read(inputStream: InputStream, bytes: Array[Byte] = internalBuffer()): Double = {
    inputStream.read(bytes)
    toDouble(bytes)
  }

  def sizeInBytes: Long = {
    bufferSize
  }
}

/**
 * Serialization of longs using java ByteBuffer
 */
object LongSerializer {

  val bufferSize = 8

  def internalBuffer(): Array[Byte] = {
    Array.ofDim[Byte](bufferSize)
  }

  def toByteArray(value: Long, bytes: Array[Byte] = internalBuffer()): Array[Byte] = {
    ByteBuffer.wrap(bytes).putLong(value)
    bytes
  }

  def toLong(bytes: Array[Byte]): Long = {
    return ByteBuffer.wrap(bytes).getLong()
  }

  def write(outputStream: OutputStream, value: Long, bytes: Array[Byte] = internalBuffer()): Unit = {
    outputStream.write(toByteArray(value, bytes))
  }

  def read(inputStream: InputStream, bytes: Array[Byte] = internalBuffer()): Long = {
    inputStream.read(bytes)
    toLong(bytes)
  }

  def sizeInBytes: Long = {
    bufferSize
  }
}

/**
 * Serialization of double array using java ByteBuffer
 */
object DoubleArraySerializer {

  def writeToBufferWithoutArrayLength(values: Array[Double], output: Array[Byte]): Unit = {
    val buff = ByteBuffer.wrap(output)
    var i = 0
    while (i < values.length) {
      buff.putDouble(values(i))
      i += 1
    }
  }

  def write(outputStream: OutputStream, values: Array[Double]): Unit = {
    IntSerializer.write(outputStream, values.length)
    val tmpBuffer = DoubleSerializer.internalBuffer()
    var i = 0
    while (i < values.length) {
      DoubleSerializer.write(outputStream, values(i), tmpBuffer)
      i += 1
    }
  }

  def read(inputStream: InputStream): Array[Double] = {
    val len = IntSerializer.read(inputStream)
    val values = Array.ofDim[Double](len)
    val tmpBuffer = DoubleSerializer.internalBuffer()
    var i = 0
    while (i < len) {
      values(i) = DoubleSerializer.read(inputStream, tmpBuffer)
      i += 1
    }
    values
  }

  def sizeInBytes(input: Array[Double]): Long = {
    IntSerializer.sizeInBytes + DoubleSerializer.sizeInBytes * input.length
  }
}

/**
 * Serialization of int array using java ByteBuffer
 */
object IntArraySerializer {
  def write(outputStream: OutputStream, values: Array[Int]): Unit = {
    IntSerializer.write(outputStream, values.length)
    val tmpBuffer = IntSerializer.internalBuffer()
    var i = 0
    while (i < values.length) {
      IntSerializer.write(outputStream, values(i), tmpBuffer)
      i += 1
    }
  }

  def read(inputStream: InputStream): Array[Int] = {
    val len = IntSerializer.read(inputStream)
    val values = Array.ofDim[Int](len)
    val tmpBuffer = IntSerializer.internalBuffer()
    var i = 0
    while (i < len) {
      values(i) = IntSerializer.read(inputStream, tmpBuffer)
      i += 1
    }
    values
  }

  def sizeInBytes(input: Array[Int]): Long = {
    IntSerializer.sizeInBytes + IntSerializer.sizeInBytes * input.length
  }
}

/**
 * Serialization of float array using java ByteBuffer
 */
object FloatArraySerializer {
  def write(outputStream: OutputStream, values: Array[Float]): Unit = {
    IntSerializer.write(outputStream, values.length)
    val tmpBuffer = FloatSerializer.internalBuffer()
    var i = 0
    while (i < values.length) {
      FloatSerializer.write(outputStream, values(i), tmpBuffer)
      i += 1
    }
  }

  def read(inputStream: InputStream): Array[Float] = {
    val len = IntSerializer.read(inputStream)
    val values = Array.ofDim[Float](len)
    val tmpBuffer = FloatSerializer.internalBuffer()
    var i = 0
    while (i < len) {
      values(i) = FloatSerializer.read(inputStream, tmpBuffer)
      i += 1
    }
    values
  }

  def sizeInBytes(input: Array[Float]): Long = {
    IntSerializer.sizeInBytes + FloatSerializer.sizeInBytes * input.length
  }
}

/**
 * Serialization of byte array using java ByteBuffer
 */
object ByteArraySerializer {
  def write(outputStream: OutputStream, values: Array[Byte]): Unit = {
    IntSerializer.write(outputStream, values.length)
    outputStream.write(values)
  }

  def read(inputStream: InputStream): Array[Byte] = {
    val len = IntSerializer.read(inputStream)
    val values = Array.ofDim[Byte](len)
    inputStream.read(values)
    values
  }

  def sizeInBytes(input: Array[Byte]): Long = {
    IntSerializer.sizeInBytes + input.length
  }
}

/**
 * Serialization of short array using java ByteBuffer
 */
object ShortArraySerializer {
  def write(outputStream: OutputStream, values: Array[Short]): Unit = {
    IntSerializer.write(outputStream, values.length)
    val tmpBuffer = ShortSerializer.internalBuffer()
    var i = 0
    while (i < values.length) {
      ShortSerializer.write(outputStream, values(i), tmpBuffer)
      i += 1
    }
  }

  def read(inputStream: InputStream): Array[Short] = {
    val len = IntSerializer.read(inputStream)
    val values = Array.ofDim[Short](len)
    val tmpBuffer = ShortSerializer.internalBuffer()
    var i = 0
    while (i < len) {
      values(i) = ShortSerializer.read(inputStream, tmpBuffer)
      i += 1
    }
    values
  }

  def sizeInBytes(input: Array[Short]): Long = {
    IntSerializer.sizeInBytes + ShortSerializer.sizeInBytes * input.length
  }
}

/**
 * Serialization of long array using java ByteBuffer
 */
object LongArraySerializer {
  def write(outputStream: OutputStream, values: Array[Long]): Unit = {
    IntSerializer.write(outputStream, values.length)
    val tmpBuffer = LongSerializer.internalBuffer()
    var i = 0
    while (i < values.length) {
      LongSerializer.write(outputStream, values(i), tmpBuffer)
      i += 1
    }
  }

  def read(inputStream: InputStream): Array[Long] = {
    val len = IntSerializer.read(inputStream)
    val values = Array.ofDim[Long](len)
    val tmpBuffer = LongSerializer.internalBuffer()
    var i = 0
    while (i < len) {
      values(i) = LongSerializer.read(inputStream, tmpBuffer)
      i += 1
    }
    values
  }

  def sizeInBytes(input: Array[Long]): Long = {
    IntSerializer.sizeInBytes + LongSerializer.sizeInBytes * input.length
  }
}