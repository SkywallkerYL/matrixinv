package FFT

import chisel3._
import chisel3.experimental._
import chisel3.util._
import hardfloat._
import scala.math._

class TopIO extends Bundle with Config {
  val dIn = Input(Vec(FFTparallel_r * radix, if(use_float) new IEEEComplex else new MyFixComplex))
  val dOut = Output(Vec(FFTparallel_r * radix, if(use_float) new IEEEComplex else new MyFixComplex))
  val din_valid = Input(new Bool())
  val dout_valid = Output(new Bool())
  val busy = Output(new Bool())
}

object floatConvert {
  def float2HPF(data:Float):Int = {
    val intBits = java.lang.Float.floatToIntBits(data)
    var resultBits = ((intBits & 0xc0000000) >> 16) + ((intBits & 0x07ffe000) >> 13)
    if((intBits & 0x40000000) == 0) {
      if(((intBits & 0x38000000) != 0x38000000)) {
        //println(s"the float32 to half-precision float is negative overloaded\n")
        //println(s"the float is ${data}")
        //println(s"the float format is ${intBits.toHexString}")
        resultBits = 0
      }
    } else {
      if(((intBits & 0x38000000) != 0) | ((intBits & 0x07800000) == 0x07800000)) {
        println(s"the float32 to half-precision float is positive overloaded")
        println(s"the float is ${data}")
        println(s"the float format is ${intBits.toHexString}")
      }
    }
    //println(s"input is ${intBits.toHexString}\n")
    //println(s"first part is ${((intBits & 0xc0000000) >> 16).toHexString}")
    //println(s"second part is ${((intBits & 0x07ffe000) >> 13).toHexString}")
    //println(s"half is ${(((intBits & 0xc0000000) >> 16) + ((intBits & 0x07ffe000) >> 13)).toHexString}")
    resultBits
  }

  def HPF2float(data:Int):Float = {
    var temp:Int = 0
    if((data & 0x4000) == 0) {
      temp = (((data & 0xc000).toLong << 16) + ((0x7) << 27) + ((data & 0x3fff) << 13)).toInt
    } else {
      temp = (((data & 0xc000).toLong << 16) + ((data & 0x3fff) << 13)).toInt
    }
    //println(s"first part is ${((data & 0xc000) << 16).toHexString}")
    //println(s"second part is ${((data & 0x3fff) << 13).toHexString}")
    //println(s"third part is ${((0x7) << 27).toHexString}")
    //println(s"half is ${(temp).toHexString}")
    java.lang.Float.intBitsToFloat(temp)
  }
}
