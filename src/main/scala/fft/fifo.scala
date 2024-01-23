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
class RAMVERILOG extends BlackBox with HasBlackBoxInline with Config{
    val io = IO(new Bundle {
        val clock	=	Input(Clock())	
        val rst   = Input(Bool())	      
        val awready = Output(Bool()) 
        val awvalid = Input(Bool())
        val awid    = Input(UInt(4.W))
        val awaddr  = Input(UInt(32.W))
        val awlen   = Input(UInt(8.W))
        val awsize  = Input(UInt(3.W))
        val awburst = Input(UInt(2.W))
        val wready  = Output(Bool())
        val wvalid  = Input(Bool())
        val wdata   = Input(UInt(64.W))
        val wstrb   = Input(UInt(8.W))
        val wlast   = Input(Bool())
        val bready  = Input(Bool())
        val bvalid  = Output(Bool())
        val bid     = Output(UInt(4.W))
        val bresp   = Output(UInt(2.W))
        val arready = Output(Bool())
        val arvalid = Input(Bool())
        val arid    = Input(UInt(4.W))
        val araddr  = Input(UInt(32.W))
        val arlen   = Input(UInt(8.W))
        val arsize  = Input(UInt(3.W))
        val arburst = Input(UInt(2.W))
        val rready  = Input(Bool())
        val rvalid  = Output(Bool())
        val rid     = Output(UInt(4.W))
        val rresp   = Output(UInt(2.W))
        val rdata   = Output(UInt(64.W))
        val rlast   = Output(Bool())  
    })
    setInline("/RAMVERILOG.v",
           """
           |module RAMVERILOG
           |    (  clock				      ,
           |       rst                ,
           |	     awready            ,
           |       awvalid            ,
           |       awid               ,
           |       awaddr             ,
           |       awlen              ,
           |       awsize             ,
           |       awburst            ,
           |       wready             ,
           |       wvalid             ,
           |       wdata              ,
           |       wstrb              ,
           |       wlast              ,
           |       bready             ,
           |       bvalid             ,
           |       bid                ,
           |       bresp              ,
           |       arready            ,
           |       arvalid            ,
           |       arid               ,
           |       araddr             ,
           |       arlen              ,
           |       arsize             ,
           |       arburst            ,
           |       rready             ,
           |       rvalid             ,
           |       rid                ,
           |       rresp              ,
           |       rdata              ,
           |       rlast              
           |    );
           |      input   clock				  ;
           |	    input   rst			      ;
           |      output  awready ;           
           |      input  awvalid ;           
           |      input [3:0] awid    ;           
           |      input [31:0] awaddr  ;           
           |      input [7:0] awlen   ;           
           |      input [2:0] awsize  ;           
           |      input [1:0] awburst ;           
           |      output  wready  ;           
           |      input  wvalid  ;           
           |      input  [63:0]wdata   ;           
           |      input  [7:0] wstrb   ;           
           |      input  wlast   ;           
           |      input  bready  ;           
           |      output  bvalid  ;           
           |      output [3:0] bid     ;           
           |      output [1:0] bresp   ;           
           |      output  arready ;           
           |      input  arvalid ;           
           |      input  [3:0] arid    ;           
           |      input  [31:0] araddr  ;           
           |      input  [7:0] arlen   ;           
           |      input  [2:0] arsize  ;           
           |      input  [1:0] arburst ;           
           |      input  rready  ;           
           |      output  rvalid  ;           
           |      output [3:0] rid     ;           
           |      output [1:0] rresp   ;           
           |      output [63:0] rdata   ;           
           |      output  rlast   ;           
           |    
           |SRAMSIM sramsim_1(
           |	.clock               (clock ),   
           |  .reset               (rst   ),   
           |  .io_Sram_ar_valid    (arvalid),   
           |  .io_Sram_ar_bits_addr(araddr),   
           |  .io_Sram_r_ready     (rready),   
           |  .io_ar_len           (arlen),   
           |  .io_ar_size          (arsize),   
           |  .io_ar_burst         (arburst),   
           |  .io_Sram_aw_valid    (awvalid),   
           |  .io_Sram_aw_bits_addr(awaddr),   
           |  .io_aw_len           (awlen),   
           |  .io_aw_size          (awsize),   
           |  .io_aw_burst         (awburst),   
           |  .io_Sram_w_valid     (wvalid),   
           |  .io_Sram_w_bits_data (wdata),   
           |  .io_Sram_w_bits_strb (wstrb),   
           |  .io_b_ready          (bready), 	
           |  .io_b_valid          (bvalid), 	
           |  .io_b_bresp          (bresp), 	 
           |  .io_Sram_ar_ready    (arready),   
           |  .io_Sram_r_valid     (rvalid),   
           |  .io_Sram_r_bits_data (rdata),   
           |  .io_Sram_r_rresp     (rresp), 	
           |  .io_Sram_r_bits_last (rlast),   
           |  .io_Sram_aw_ready    (awready),   
           |  .io_Sram_w_ready     (wready) 
           |);
           |endmodule
           """.stripMargin)
}


class ramtop extends Module with Config {
  val io = IO(new Bundle {
    // read pix 
    // write bit 
    //input 
    val axi = Flipped(new AxiRead)
  })
  val ram = Module(new RAMVERILOG)
  //ram.io.awready <> io.axi.awready  
  ram.io.awvalid := false.B
  ram.io.awid    := 0.U  
  ram.io.awaddr  := 0.U  
  ram.io.awlen   := 0.U  
  ram.io.awsize  := 0.U  
  ram.io.awburst := 0.U  
  //ram.io.wready  <> io.axi.wready   
  ram.io.wvalid  := false.B //<> io.axi.wvalid   
  ram.io.wdata   := 0.U//<> io.axi.wdata    
  ram.io.wstrb   := 0.U //<> io.axi.wstrb    
  ram.io.wlast   := 0.U //<> io.axi.wlast    
  ram.io.bready  := false.B //<> io.axi.bready   
  //ram.io.bvalid  <> io.axi.bvalid   
  //ram.io.bid     <> io.axi.bid      
  //ram.io.bresp   <> io.axi.bresp    
  ram.io.arready <> io.axi.arready  
  ram.io.arvalid <> io.axi.arvalid  
  ram.io.arid    := 0.U //<> io.axi.arid     
  ram.io.araddr  <> io.axi.araddr   
  ram.io.arlen   <> io.axi.arlen    
  ram.io.arsize  :=3.U //<> io.axi.arsize   
  ram.io.arburst :=0.U //<> io.axi.arburst  
  ram.io.rready  <> io.axi.rready   
  ram.io.rvalid  <> io.axi.rvalid   
  //ram.io.rid     := 0.U //<> io.axi.rid      
  //ram.io.rresp   :=<> io.axi.rresp    
  ram.io.rdata   <> io.axi.rdata    
  ram.io.rlast   <> io.axi.rlast    
  ram.io.clock := clock 
  ram.io.rst   := reset.asBool

}