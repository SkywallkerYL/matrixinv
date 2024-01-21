package FFT

import scala.math._
import chisel3._
import chisel3.util._
trait Config {
  def exp(f: Int) = f match {
    case 16 => 5
    case 32 => 8
    case 64 => 11
  }

  def sig(f: Int) = f match {
    case 16 => 11
    case 32 => 24
    case 64 => 53
  }
  val DataWidthIn  = 32
  //val FloatWidth = DataWidth/2


  val float_point_format = 32   // support 16, 32, 64
  val expWidth = exp(float_point_format)
  val sigWidth = sig(float_point_format)
  val use_float = true //当use_float=true时使用浮点数, 否则使用定点数
  //这个参数不要动。。。

  val use_ip = true//是否使用Xilinx的浮点运算IP
//config of fixedpoint data format
  val FixDataWidth = 26
  val BinaryPoint = 10
  // ParallelNum仅支持1 2 4 8 ...
  val ParallelNum = 1 
  val Mshift = if(ParallelNum==1) 0 else log2Ceil(ParallelNum)
  //单个浮点运算的延迟
  val MulLatency = 8
  val AddLatency = 11 
  //复数乘法延迟为 MulLatency + AddLatency 
  //复数加法延迟为 AddLatency
  val ComplexMulLatency = MulLatency + AddLatency
  val ComplexAddLatency = AddLatency
  val Nmax = 12
  val Mmax = 32
  
  val Nwidth = log2Ceil(Nmax)
  val Mwidth = log2Ceil(Mmax)
  val MemDepthWidth = Nwidth+Mwidth
  val MemDepth =  pow(2, MemDepthWidth).toInt
  val PulseWidth = 16
  val AXIDATAWIDTH = 64 
  val AXISTRBWIDTH = 8
  val AXIADDRWIDTH = 36
  val AXILENWIDTH  = 8
  val AXISIZEWIDTH = 3
  val AXIBURSTWIDTH= 2 
  val AXIIDWIDTH   = 4
  val AXIRESPWIDTH = 2
// config of construct
// support all parallel data (datalength = 1)
// FFTstage - FFTparallel must > 0
  val radix = 2  //radix of the FFT,supprot 2, 4, 8
  val FFTstage = 6// FFT stages
  val FFTparallel = 0 // the really parallel is radix ^ FFTparallel
  val useGauss = false // whether use gauss multiplier
  val do_reorder = false // true: output is one bit serial and all in order
//  val useParallel = true // parallel input or serial input

//parameters
  val FFTlength = pow(radix, FFTstage).toInt
  val FFTparallel_r = pow(radix, FFTparallel).toInt
  val datalength = (FFTlength / (radix * FFTparallel_r))
  val DataWidth = if(use_float) float_point_format+1  else FixDataWidth

  val SRC_DATA_AXI_THRESHOLD    = 256
  val SINGLE_FLOAT_SUB_LATENCY  = 6
  val SINGLE_FLOAT_ADD_LATENCY  = 6
  val SINGLE_FLOAT_MULT_LATENCY = 4
  val BUTTERFLY_AXI_DLY = SINGLE_FLOAT_SUB_LATENCY + 2
}
object Config extends Config {}
