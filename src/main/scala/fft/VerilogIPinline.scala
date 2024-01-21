package FFT

import chisel3._
import chisel3.util._
import chisel3.experimental._
class FloatIpIO extends Bundle with Config {
  val clk       = Input(Clock())  // input wire clk
  val rst       = Input(Bool())   // input wire rst
  val a      = Flipped(new AxiStreamSingle)
  val b      = Flipped(new AxiStreamSingle)
  val m      = new AxiStreamSingle
}
class floataddinline extends BlackBox with HasBlackBoxInline {
    val io = IO(new Bundle {
        val io = new FloatIpIO
    })
    setInline("/floataddinline.v",
           """
           |module floataddinline(
           |  input                   io_clk       ,
           |  input                   io_rst       ,
           |  input  [31:0]           io_a_data     ,
           |  input                   io_a_valid    ,
           |  input                   io_a_last     ,
           |  input  [31:0]           io_b_data     ,
           |  input                   io_b_valid    ,
           |  input                   io_b_last     ,
           |  output  [31:0]          io_m_data     ,
           |  output                  io_m_valid    ,
           |  output                  io_m_last     
           |    );
           | 
           |
           |fp_add add(
           |  .aclk					(io_clk						), // input wire aclk
           |  .aresetn				(!io_rst					), // input wire aresetn
           |  .s_axis_a_tvalid		(io_a_valid            		), // input wire s_axis_a_tvalid
           |  .s_axis_a_tdata		(io_a_data               	), // input wire [31 : 0] s_axis_a_tdata
           |  .s_axis_a_tlast		(1'b0						), // input wire s_axis_a_tlast
           |  .s_axis_b_tvalid		(io_b_valid  			    ), // input wire s_axis_b_tvalid
           |  .s_axis_b_tdata		(io_b_data          		), // input wire [31 : 0] s_axis_b_tdata
           |  .s_axis_b_tlast		(1'b0						), // input wire s_axis_b_tlast
           |  .m_axis_result_tvalid	(io_m_valid                 ), // output wire m_axis_result_tvalid
           |  .m_axis_result_tdata	(io_m_data	                ), // output wire [31 : 0] m_axis_result_tdata
           |  .m_axis_result_tlast	(io_m_last                  ) // output wire m_axis_result_tlast
           |);
           |
           |
           |
           |endmodule
           """.stripMargin)
}
class FloatAddwithIP extends Module with Config {
  val io = IO(new FloatOperationIO)
  //
  val float_adder = Module(new floataddinline)
  float_adder.io.io.clk := clock
  float_adder.io.io.rst := reset.asBool
  float_adder.io.io.a.valid := true.B 
  float_adder.io.io.a.data  := io.op1 
  float_adder.io.io.a.last  := false.B  
  float_adder.io.io.b.valid := true.B 
  float_adder.io.io.b.data  := io.op2 
  float_adder.io.io.b.last  := false.B 
  io.res := float_adder.io.io.m.data
}

class floatsubinline extends BlackBox with HasBlackBoxInline {
    val io = IO(new Bundle {
        val io = new FloatIpIO
    })
    setInline("/floatsubinline.v",
           """
           |module floatsubinline(
           |  input                   io_clk       ,
           |  input                   io_rst       ,
           |  input  [31:0]           io_a_data     ,
           |  input                   io_a_valid    ,
           |  input                   io_a_last     ,
           |  input  [31:0]           io_b_data     ,
           |  input                   io_b_valid    ,
           |  input                   io_b_last     ,
           |  output  [31:0]          io_m_data     ,
           |  output                  io_m_valid    ,
           |  output                  io_m_last     
           |    );
           | 
           |
           |fp_sub sub(
           |  .aclk					(io_clk						), // input wire aclk
           |  .aresetn				(!io_rst					), // input wire aresetn
           |  .s_axis_a_tvalid		(io_a_valid            		), // input wire s_axis_a_tvalid
           |  .s_axis_a_tdata		(io_a_data               	), // input wire [31 : 0] s_axis_a_tdata
           |  .s_axis_a_tlast		(1'b0						), // input wire s_axis_a_tlast
           |  .s_axis_b_tvalid		(io_b_valid  			    ), // input wire s_axis_b_tvalid
           |  .s_axis_b_tdata		(io_b_data          		), // input wire [31 : 0] s_axis_b_tdata
           |  .s_axis_b_tlast		(1'b0						), // input wire s_axis_b_tlast
           |  .m_axis_result_tvalid	(io_m_valid                 ), // output wire m_axis_result_tvalid
           |  .m_axis_result_tdata	(io_m_data	                ), // output wire [31 : 0] m_axis_result_tdata
           |  .m_axis_result_tlast	(io_m_last                  ) // output wire m_axis_result_tlast
           |);
           |
           |
           |
           |endmodule
           """.stripMargin)
}
class FloatSubwithIP extends Module with Config {
  val io = IO(new FloatOperationIO)
  //
  val float_adder = Module(new floatsubinline)
  float_adder.io.io.clk := clock
  float_adder.io.io.rst := reset.asBool
  float_adder.io.io.a.valid := true.B 
  float_adder.io.io.a.data  := io.op1 
  float_adder.io.io.a.last  := false.B  
  float_adder.io.io.b.valid := true.B 
  float_adder.io.io.b.data  := io.op2 
  float_adder.io.io.b.last  := false.B 
  io.res := float_adder.io.io.m.data
}

class floatmulinline extends BlackBox with HasBlackBoxInline {
    val io = IO(new Bundle {
        val io = new FloatIpIO
    })
    setInline("/floatmulinline.v",
           """
           |module floatmulinline(
           |  input                   io_clk       ,
           |  input                   io_rst       ,
           |  input  [31:0]           io_a_data     ,
           |  input                   io_a_valid    ,
           |  input                   io_a_last     ,
           |  input  [31:0]           io_b_data     ,
           |  input                   io_b_valid    ,
           |  input                   io_b_last     ,
           |  output  [31:0]          io_m_data     ,
           |  output                  io_m_valid    ,
           |  output                  io_m_last     
           |    );
           | 
           |
           |fp_mul mul(
           |  .aclk					(io_clk						), // input wire aclk
           |  .aresetn				(!io_rst					), // input wire aresetn
           |  .s_axis_a_tvalid		(io_a_valid            		), // input wire s_axis_a_tvalid
           |  .s_axis_a_tdata		(io_a_data               	), // input wire [31 : 0] s_axis_a_tdata
           |  .s_axis_a_tlast		(1'b0						), // input wire s_axis_a_tlast
           |  .s_axis_b_tvalid		(io_b_valid  			    ), // input wire s_axis_b_tvalid
           |  .s_axis_b_tdata		(io_b_data          		), // input wire [31 : 0] s_axis_b_tdata
           |  .s_axis_b_tlast		(1'b0						), // input wire s_axis_b_tlast
           |  .m_axis_result_tvalid	(io_m_valid                 ), // output wire m_axis_result_tvalid
           |  .m_axis_result_tdata	(io_m_data	                ), // output wire [31 : 0] m_axis_result_tdata
           |  .m_axis_result_tlast	(io_m_last                  ) // output wire m_axis_result_tlast
           |);
           |
           |
           |
           |endmodule
           """.stripMargin)
}
class FloatMulwithIP extends Module with Config {
  val io = IO(new FloatOperationIO)
  //
  val float_adder = Module(new floatmulinline)
  float_adder.io.io.clk := clock
  float_adder.io.io.rst := reset.asBool
  float_adder.io.io.a.valid := true.B 
  float_adder.io.io.a.data  := io.op1 
  float_adder.io.io.a.last  := false.B  
  float_adder.io.io.b.valid := true.B 
  float_adder.io.io.b.data  := io.op2 
  float_adder.io.io.b.last  := false.B 
  io.res := float_adder.io.io.m.data
}