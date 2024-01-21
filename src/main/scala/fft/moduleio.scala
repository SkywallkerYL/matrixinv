package FFT


import chisel3._
import hardfloat._
import chisel3.util._
class AxiStream extends Bundle with Config{
  val valid  = Output(Bool())
  val data   = Output(UInt((DataWidthIn*2).W))
  val ready  = Input(Bool())
  val last   = Output(Bool())
}
class AxiStreamSingle extends Bundle with Config{
  val valid  = Output(Bool())
  val data   = Output(UInt((DataWidthIn).W))
  val last   = Output(Bool())
}
class AxiStream256 extends Bundle with Config{
  val valid  = Output(Bool())
  val data   = Output(UInt((256).W))
  val ready  = Input(Bool())
  val last   = Output(Bool())
}

class AxiRead extends Bundle with Config{
  val arready = Input(Bool())
  val arvalid = Output(Bool())
  val araddr  = Output(UInt(AXIADDRWIDTH.W))
  val arlen   = Output(UInt(AXILENWIDTH.W))

  val rready  = Output(Bool())
  val rvalid  = Input(Bool())
  val rdata   = Input(UInt(AXIDATAWIDTH.W))
  val rlast   = Input(Bool())
}

