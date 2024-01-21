package FFT
import chisel3._
import chisel3.stage.ChiselStage

object generator extends App with Config {
  //(new ChiselStage).emitVerilog(new FFTtop,Array("--target-dir",s"generated/${FFTlength}Point_${FFTparallel}parallel_${use_float}float_${DataWidth-1}width/"))
  (new ChiselStage).emitVerilog(new DDrReadModule,Array("--target-dir",s"build/"))
  // DDrReadModule MatrixInvfullTop
  //(new ChiselStage).emitVerilog(new Switch(1, MyFixComplex))
  //val data2 = (VecInit(Seq.fill(radix)(0.S((2 * DataWidth).W).asTypeOf(MyComplex))))
  //def top = new FFTtop

  //val generator1 = Seq(
    //chisel3.stage.ChiselGeneratorAnnotation(() => top),
    //firrtl.stage.RunFirrtlTransformAnnotation(new AddModulePrefix()),
    //ModulePrefixAnnotation("ysyx_22050550_")
  //)
  //(new chisel3.stage.ChiselStage).execute(args, generator1) 
}
