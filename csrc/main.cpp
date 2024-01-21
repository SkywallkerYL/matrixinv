#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <stdlib.h>
#include "common.h"
#include "testfunc.h"
#include "simualte.h"
int main(int argc , char* argv[]) {
	sim_init();

	reset(5);
#if TESTMODULE == 1
	checknodetest(100000);
#elif TESTMODULE == 2
	//not use 
	variablenodetest(1000000);
#elif TESTMODULE == 3
	//not use 
	ProcessUnitTest(10000);
#elif TESTMODULE == 4
	DecoderTest();
#elif TESTMODULE == 5
	//ber = 0.05的 p   如果din是1的话，输出应该是0.95
	uint64_t p0 = 0x310adfe56a85f400;
	uint64_t p1 = 0x179e610acab3f600;
	uint64_t p2 = 0x0ccd96e5466e1300;
	uint64_t p3 = 0x065cabb21c0bec40;
	uint64_t p4 = 0x0200490338f18340;
	//注意matlab生成的 p是小端序  即 p = {p4,p3,p2,p1,p0} 
	gngwrappertest(10000,0.05,p0,p1,p2,p3,p4);
#elif TESTMODULE == 6
	toptest();
#elif TESTMODULE == 7
#if READLLR
	//DecoderTest();
	ReadfileDecode();
#else 
	DecoderTest();
#endif
	//开这个可能会有一个Bug 就是打波形发现Checknode没有正常更新min 和submin  
	//但是看Verilog代码应该是会正常更新的  不知道是不是Verilator的BUG
	//但是跑性能图和软件可以对上     
	//cppDiffDecoderTest();
//	cppDecoderTest();
//	diffDecoderTest();
#elif TESTMODULE == 8
	toptest();
#elif TESTMODULE == 9 
	toptest();
#elif TESTMODULE == 10 
	//printf("jjjjj\n");
	toptest();
#elif TESTMODULE == 11 
	//printf("jjjjj\n");
	toptest();
#elif TESTMODULE == 12 
	//printf("jjjjj\n");
	toptest();
#endif 
	sim_exit();
	return 0;
}
