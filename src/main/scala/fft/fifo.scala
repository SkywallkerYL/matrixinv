package FFT

import chisel3._
import chisel3.experimental._
import chisel3.util._
class SyncFifoIO extends Bundle with Config {
  val clk       = Input(Clock())  // input wire clk
  val rst       = Input(Bool())   // input wire rst
  val din       = Input(UInt(AXIDATAWIDTH.W))  // input wire [63 : 0] din
  val wr_en     = Input(Bool())  // input wire wr_en
  val rd_en     = Input(Bool())  // input wire rd_en
  val dout      = Output(UInt(AXIDATAWIDTH.W))  // output wire [63 : 0] dout
  val full      = Output(Bool())  // output wire full
  val empty     = Output(Bool())  // output wire empty
  val valid     = Output(Bool())
  val datacount = Output(UInt(7.W))
}

class fifoinline extends BlackBox with HasBlackBoxInline {
    val io = IO(new Bundle {
        val fifo = new SyncFifoIO
    })
    setInline("/fifoinline.v",
           """
           |module fifoinline(
           |  input                   fifo_clk       ,
           |  input                   fifo_rst       ,
           |  input  [255:0]           fifo_din       ,
           |  input                   fifo_wr_en     ,
           |  input                   fifo_rd_en     ,
           |  output [255:0]           fifo_dout      ,
           |  output                  fifo_full      ,
           |  output                  fifo_empty     ,
           |  output                  fifo_valid     ,
           |  output [6:0]            fifo_datacount
           |    );
           | 
           |
           |syncfifo_64b_512 fifo_save_half_point64 (
           |.clk	  (fifo_clk       ),      // input wire clk
           |.rst	  (fifo_rst       ),      // input wire rst
           |.din	  (fifo_din       		),      // input wire [63 : 0] din
           |.wr_en	  (fifo_wr_en     	),  	// input wire wr_en
           |.rd_en	  (fifo_rd_en     ),  	// input wire rd_en
           |.dout	  (fifo_dout      		),    	// output wire [63 : 0] dout
           |.full	  (fifo_full       ),    	// output wire full
           |.empty	  (fifo_empty      )  	// output wire empty
           |);
           |assign fifo_valid     = 0;
           |assign fifo_datacount = 0;
           |
           |
           |
           |endmodule
           """.stripMargin)
}

class WriterIO(size: Int) extends Bundle {
    val write = Input(Bool())
    val full = Output(Bool())
    val din = Input(UInt(size.W))
}
class ReaderIO(size: Int) extends Bundle {
    val read = Input(Bool())
    val empty = Output(Bool())
    val dout = Output(UInt(size.W))
}
class fifo(val size : Int, val width : Int) extends Module {
  val io = IO(new Bundle {
    val dataIn = Input(UInt(width.W))
    val dataOut = Output(UInt(width.W))
    val writeFlag = Input(Bool())
    val readFlag = Input(Bool())
    val full = Output(Bool())
    val empty = Output(Bool())
  })

  val count = RegInit(0.U((log2Ceil(size)+1).W))
  val mem = Mem(size, UInt(width.W))
  val wPointer = RegInit(0.U((log2Ceil(size)).W))
  val rPointer = RegInit(0.U((log2Ceil(size)).W))
  val dataOut = RegInit(0.U(width.W))

  def indexAdd(index : UInt) : UInt = {
      Mux(index === (size - 1).U, 0.U, index + 1.U)
  }

  when(io.writeFlag === true.B && io.readFlag === true.B) {
    when(count === 0.U) { dataOut := io.dataIn }
    .otherwise {
      dataOut := mem(rPointer)
      rPointer := indexAdd(rPointer)
      mem(wPointer) := io.dataIn
      wPointer := indexAdd(wPointer)
    } 
  } .elsewhen (io.writeFlag === true.B && io.readFlag === false.B) {
    dataOut := 0.U
    when(count < size.U) {
      mem(wPointer) := io.dataIn
      wPointer := indexAdd(wPointer)
      count := count + 1.U
    }
  } .elsewhen (io.writeFlag === false.B && io.readFlag === true.B) {
    when(count > 0.U) {
      dataOut := mem(rPointer)
      rPointer := indexAdd(rPointer)
      count := count - 1.U
    } .otherwise {
      dataOut := 0.U
    }
  } .otherwise {
    dataOut := 0.U
  }

  io.dataOut := dataOut
  io.full := (size.U === count)
  io.empty := (count === 0.U)
}
class fifoinst(val size : Int, val width : Int) extends Module with Config{
  val io = IO(new Bundle {
        val fifo = new SyncFifoIO
  })
  val bubblefifo = Module(new fifo(size,width) )
  io.fifo.dout := bubblefifo.io.dataOut
  io.fifo.full := bubblefifo.io.full 
  io.fifo.empty:= bubblefifo.io.empty
  io.fifo.datacount := 0.U 
  io.fifo.valid := false.B 
  bubblefifo.io.dataIn := io.fifo.din 
  bubblefifo.io.writeFlag := io.fifo.wr_en
  bubblefifo.io.readFlag := io.fifo.rd_en
}