package FFT

import chisel3._
import chisel3.util._
import chisel3.experimental._
import scala.math._
class MatrixInvfullTop extends Module with Config {
  val io = IO(new Bundle{
    val conFig = Flipped(new AxiStream)
    val dIn    = Flipped(new AxiStream)
    val dOut   = (new AxiStream)
    val out = Output(new IEEEComplex)
  })
  print(MemDepth)
  print(" \n")
  print(Mshift)
  print(" \n")
  val matrixinv = Seq.fill(ParallelNum)(Module (new MatrixInvCore))
  val N = io.conFig.data(2*DataWidthIn-1,DataWidthIn)
  val M = io.conFig.data(DataWidthIn-1,0) 
  val choosenumber = RegInit(0.U((log2Ceil(ParallelNum+1)).W))
  io.dOut <> matrixinv(0).io.dOut
  io.out.re := io.dOut.data(DataWidthIn-1,0)
  io.out.im := io.dOut.data(2*DataWidthIn-1,DataWidthIn)
  val lastflag = VecInit(Seq.fill(ParallelNum)(false.B))
  val reallast = lastflag.reduce(_|_)
  for(i <- 0 until ParallelNum){

    matrixinv(i).io.dOut.ready := false.B 
    matrixinv(i).io.conFig <> io.conFig 
    matrixinv(i).io.dIn <> io.dIn
    matrixinv(i).io.choosevalid := false.B 
    matrixinv(i).io.Mbase  := (M>>Mshift.U)*i.U
    when(choosenumber === i.U){
      matrixinv(i).io.choosevalid := true.B 
      io.dOut.data :=  matrixinv(i).io.dOut.data
      matrixinv(i).io.dOut.ready := io.dOut.ready 
      io.dOut.valid := matrixinv(i).io.dOut.valid
    }
    lastflag(i) := matrixinv(i).io.dOut.last
    //matrixinv(i).io.dOut <> io.dOut(i)
  
  }
  io.dOut.last := reallast && (choosenumber=== (ParallelNum-1).U)
  val idle :: process :: Nil = Enum(2)
  val controlstate = RegInit(idle)
 
  switch(controlstate){
    is(idle){
      when(io.conFig.valid){
        controlstate := process
        choosenumber := 0.U 
      }
    }
    is(process){
      when(matrixinv(ParallelNum-1).io.dOut.last){
        controlstate := idle
      }
      when(reallast){
        choosenumber := choosenumber + 1.U
      }
    }
  }

  


}
/****
这个模块进行矩阵自相关的运算，
首先配置M N参数，
然后读取M*N 个矩阵元素的数值 ，默认近来的数据是按行进
然后进行输出矩阵元素的计算
进行输出
输出也默认按行输出。

****/
class MatrixInvCore extends Module with Config {
  val io = IO(new Bundle{
    val conFig = Flipped(new AxiStream) 
    val dIn    = Flipped(new AxiStream)
    val dOut   = ((new AxiStream))
    val Mbase = Input(UInt(Mwidth.W))
    val choosevalid = Input(Bool())
   // val finish = Output(Bool())
  })
  //default out 
  

  io.conFig.ready := false.B 
  io.dIn.ready := false.B 
  io.dOut.valid := false.B 
  io.dOut.last := false.B 
  //
  val complex = if(use_float) new IEEEComplex else new IntgerComplex

//假定N存在conFig 的高32位 M存在低32位
  val N = RegInit(0.U(Nwidth.W))
  val M = RegInit(0.U(Mwidth.W))
  val Mbase = RegInit(0.U(Mwidth.W))
  val ColmunCount = RegInit(0.U(Nwidth.W))
  

  val ColCount = RegInit(0.U(Nwidth.W)) 
  val MCount = RegInit(0.U(Mwidth.W))
  val NCount = RegInit(0.U(Nwidth.W))

  val MatrixRam = (SyncReadMem(MemDepth,UInt((2*DataWidthIn).W)))
  //val RamReadEn = Wire(Bool())
  val RamWriteEn = Wire(Bool())
  val RamWriteData = Wire(UInt((2*DataWidthIn).W))
  RamWriteData := io.dIn.data
  RamWriteEn := false.B 
  val RamAddrWrite  = Wire(UInt(MemDepthWidth.W))
  RamAddrWrite := NCount##MCount
  val RamAddrRead   = Wire(UInt(MemDepthWidth.W))
  val RamReadData = Wire(UInt((2*DataWidthIn).W))
  when(RamWriteEn){
    MatrixRam.write(RamAddrWrite,RamWriteData)
  }
 
  //计算Cij  i= RowcountRow j = Rowcountcol k = Colcount
  val RowCountRow = RegInit(0.U(Mwidth.W))
  val RowCountCol = RegInit(0.U(Mwidth.W)) 
  //默认读Aik 注意这个地址要提前一个周期给
  //添加了并行度的约束后，当前真正处理的行数是
  val realRow = (RowCountRow+Mbase)
  RamAddrRead := ColmunCount##realRow
  RamReadData := MatrixRam.read(RamAddrRead)

  val Areal = RamReadData(DataWidthIn-1,0) 
  val Aimag = RamReadData(2*DataWidthIn-1,DataWidthIn)
  val SubAimag = (~RamReadData(2*DataWidthIn-1)) ##RamReadData(2*DataWidthIn-2,DataWidthIn) 
  val Aik = RegInit(0.S((2*DataWidthIn).W).asTypeOf(complex))
  val Ajk = RegInit(0.S((2*DataWidthIn).W).asTypeOf(complex))
//乘法器结果延迟ComplexMulLatency个周期输出 
  val Mvalid = RegInit(false.B)
  val realMvalid = ShiftRegister(Mvalid,ComplexMulLatency)


  val FloatInAik = if (use_ip) 0.U.asTypeOf(new MyFloatComplex)  else ComplexRecode(Aik.asTypeOf(new IEEEComplex))
  val FloatInAjk = if (use_ip) 0.U.asTypeOf(new MyFloatComplex)  else  ComplexRecode(Ajk.asTypeOf(new IEEEComplex))
  val Mdata = if (use_ip) 0.U.asTypeOf(new IEEEComplex)  else  ComplexDecode(ComplexMul((FloatInAik),(FloatInAjk)).asTypeOf(new MyFloatComplex))
  //ComplexDecode
  //RegInit(0.asTypeOf(complex))
  //val realMdata = ShiftRegister(Mdata,ComplexMulLatency)
  //
  val realMdata = if (use_ip) ComplexMul((Aik),(Ajk)) else ShiftRegister(Mdata,ComplexMulLatency)

  val mulfifo = Module(new fifoinst(((Nmax)),2*DataWidthIn))
  mulfifo.io.fifo.clk := clock 
  mulfifo.io.fifo.rst := reset.asBool
  mulfifo.io.fifo.din := Cat(realMdata.im, realMdata.re)
  val mulfifoout  = Wire(new IEEEComplex)
  mulfifoout.re := mulfifo.io.fifo.dout(DataWidthIn-1,0)
  mulfifoout.im := mulfifo.io.fifo.dout(2*DataWidthIn-1,DataWidthIn)
  mulfifo.io.fifo.wr_en := realMvalid 
  mulfifo.io.fifo.rd_en := false.B 

  val Cij = RegInit(0.S((2*DataWidthIn).W).asTypeOf(complex))

  //加法器结果延迟AddLatency个周期输出 
  val FloatInCij =if (use_ip) 0.U.asTypeOf(new MyFloatComplex) else ComplexRecode(Cij.asTypeOf(new IEEEComplex))
  val Adddata =if (use_ip) 0.U.asTypeOf(new IEEEComplex) else  ComplexDecode(ComplexAdd((FloatInCij),ComplexRecode(mulfifoout)).asTypeOf(new MyFloatComplex))
  //RegInit(0.asTypeOf(complex))
  val realAdddata =if (use_ip) ComplexAdd((Cij),(mulfifoout)) else ShiftRegister(Adddata,ComplexAddLatency)
  val Addinvalid = RegInit(false.B )
  //
  val Addvalid = ShiftRegister(Addinvalid,ComplexAddLatency)

  val resultvalid = Wire(Bool())
  val finish = Wire(Bool())
  val allfinish = RegInit(false.B )
  resultvalid := false.B 
  finish := false.B 

  val idle :: getMatrix :: getAik :: getAjk :: mulComplex :: waitadd ::waitupdate:: Nil = Enum(7)
  //  0    :: 1         :: 2      :: 3      :: 4          :: 5       ::6
  val MatrixInvState = RegInit(idle)
  switch(MatrixInvState){
    is(idle){
      io.conFig.ready := true.B 
      
      Mvalid := false.B
      when(io.conFig.valid){
        N := io.conFig.data(2*DataWidthIn-1,DataWidthIn)
        M := io.conFig.data(DataWidthIn-1,0)
        Mbase := io.Mbase
        //ColmunCount := 0.U 
        MCount := 0.U  
        NCount := 0.U 
        MatrixInvState := getMatrix
      }
    }
    is(getMatrix){
      io.dIn.ready := true.B 
      when(io.dIn.valid){
        RamWriteEn := true.B 
        when(NCount === N -1.U){
          NCount := 0.U 
          when(MCount === M-1.U){
            MCount :=0.U 
            MatrixInvState := getAik 
          }.otherwise{
            MCount := MCount + 1.U 
          }
        }.otherwise{
          NCount := NCount + 1.U 
        }
      }
    }
    
    is(getAik){
      
      Mvalid := false.B 

      Aik.re := Areal.asTypeOf(complex.re)
      Aik.im := Aimag.asTypeOf(complex.im)
      //提前一个周期更改地址，
      RamAddrRead := ColmunCount##RowCountCol
      MatrixInvState := getAjk
    }
    is(getAjk){
      Ajk.re := Areal.asTypeOf(complex.re)
      //共轭  虚部去反
      Ajk.im := SubAimag.asTypeOf(complex.im)
      when(ColmunCount === N-1.U){
        //k 的遍历完成
        MatrixInvState := waitadd 
        ColmunCount:= 0.U
      }.otherwise{
        MatrixInvState := getAik
        RamAddrRead := (ColmunCount+1.U)##realRow
        ColmunCount:= ColmunCount + 1.U 
      }
      //提前送下一个地址
      
      Mvalid := true.B 
    }
    is(waitadd){
      Mvalid := false.B 
      //提前送下一个地址
      when(finish){
        MatrixInvState := idle 
      }.elsewhen(resultvalid){
        ColmunCount := 0.U 
        //注意这个时候Colmun这些还没更新，空一个状态出来更新
        MatrixInvState := waitupdate
      }
    }
    is(waitupdate){
      MatrixInvState := getAik
    }
  }
  //控制加法
  val idle1 :: waitmulti :: waitmul :: update :: Nil = Enum(4)
  val addState = RegInit(idle1)
  switch(addState){
    is(idle1){
      when(io.conFig.valid){
        addState := waitmulti
        RowCountRow := 0.U 
        RowCountCol := 0.U 
        ColCount := 0.U 
        Cij := 0.U.asTypeOf(complex)
        allfinish := false.B 
        Addinvalid := false.B 
      }
    }
    is(waitmulti){
      //fifo非空 读乘法的结果用来求和
      when(!mulfifo.io.fifo.empty){
        Addinvalid := true.B 
        mulfifo.io.fifo.rd_en := true.B 
        addState := waitmul

      }
    }
    is(waitmul){
      Addinvalid :=false.B 
      when(Addvalid){
        Cij := realAdddata
        addState := update 
      }
    }
    is(update){
      //ColCount := ColCount + 1.U 
      //addState := waitmul
      //k列的加法做完了  要更新到矩阵下一个元素
      when(ColCount === N-1.U){
        Cij := 0.U.asTypeOf(complex)
        ColCount := 0.U 
        resultvalid := true.B 
        when(RowCountCol === M-1.U){
          RowCountCol := 0.U 
          when(RowCountRow === (M>>Mshift.U)-1.U){
            //finish := true.B  
            RowCountCol := RowCountCol + 1.U
            //RowCountRow := 0.U 
            addState := idle1
            allfinish := true.B 
            finish := true.B 
          }.otherwise{
             
            addState := waitmulti
          }
          RowCountRow := RowCountRow + 1.U
        }.otherwise{
          RowCountCol := RowCountCol + 1.U 
          addState := waitmulti
        }
      }.otherwise{
        addState := waitmulti
        ColCount := ColCount + 1.U 
      }
    }
  }
  //
  val fifodata = Module(new fifoinst(Nmax*Mmax/ParallelNum,2*DataWidthIn))
  fifodata.io.fifo.clk := clock 
  fifodata.io.fifo.rst := reset.asBool
  fifodata.io.fifo.din := Cat(Cij.im, Cij.re)
  fifodata.io.fifo.wr_en := resultvalid 
  fifodata.io.fifo.rd_en := false.B 
  //控制输出握手
  val Mulres = RegInit(0.U((2*DataWidthIn).W))//.asTypeOf(complex))
  val fifooutvalid = RegInit(false.B)
  when(fifooutvalid){
    Mulres := fifodata.io.fifo.dout
  }
  //io.dOut.data(2*DataWidthIn-1,DataWidthIn) := 0.U //Mulres.im
  //io.dOut.data(DataWidthIn-1,0)             := 0.U //Mulres.re
  //寄存fifo数据，防止外部ready未拉高，而fifo的数据只会维持一个周期
  io.dOut.data := Mux(fifooutvalid,fifodata.io.fifo.dout,Mulres)
  val idle0 :: readfifo :: readfifoen :: Nil = Enum(3)
  val shakestate = RegInit(idle0)

  switch(shakestate){
    is(idle0){
      when(io.choosevalid){
        shakestate := readfifoen
      }
    }
    is(readfifo){
      io.dOut.valid:= true.B 
      fifooutvalid := false.B 
      when(io.dOut.ready){
        when(!allfinish || (!fifodata.io.fifo.empty)){
          shakestate := readfifoen
        }.otherwise{
          shakestate := idle0 
          io.dOut.last := true.B 
        }
      }
    }
    is(readfifoen){
      when(!fifodata.io.fifo.empty){
        fifodata.io.fifo.rd_en := true.B 
        fifooutvalid := true.B
        shakestate := readfifo
      }
    }
  }
}




