#ifndef CHECKNODE_H
#define CHECKNODE_H 
/*************************************************************************
	> File Name: checknode.h
	> Author: yangli
	> Mail: 577647772@qq.com 
	> Created Time: 2023年05月21日 星期日 11时06分44秒
 ************************************************************************/
#include "common.h"
#include "channel.h"
#include "simualte.h"
#include <ctime>
#include <fstream>
#include <string>
#include <sstream>
#include <cstdlib>
#include <chrono>
#include <random>
using namespace std;

#if TESTMODULE == 11

void toptest(){
	//printf("hhhh\n");
	
	//for (int i = 0; i < 11 ; i ++) {
	float re0 = 2.2;
	float im0 = 0.0;
	float re1 = 2.3;
	float im1 = 0.0;
	auto start = std::chrono::high_resolution_clock::now();

    // 执行需要计时的代码

    reset(10);
	//printf("aaaa\n");
	top->io_conFig_valid = 1;
	long long  M= 16;
	long long  N= 12;
	top->io_conFig_data  = (N<<32)| M;
	clockntimes(1);
	top->io_conFig_valid = 0;
	top->io_dIn_valid  = 1 ;
	top->io_dOut_ready = 1;
	//top->io_dIn_0_data = (((long long)(rand() % 256))<<32)| (long long)((rand() % 256));
	int rows = M;
    int cols = N;

	float realPart[rows][cols];
    float imagPart[rows][cols];
	float autoCorrelationrealPart[rows][rows];
	float autoCorrelationimagPart[rows][rows];
	std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_real_distribution<float> dist(0.0, 1.0);

	for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
            realPart[i][j] = dist(gen);//(float)(i%10);
            imagPart[i][j] = dist(gen);//(float)(j%10);
        }
    }
	std::cout << "Matrix:"<<std::endl;
	for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
            std::cout << realPart[i][j] << "+";
			std::cout << imagPart[i][j] << "i\t";
        }
        std::cout << std::endl;
    }
	for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < rows; ++j) {
            autoCorrelationrealPart[i][j] = 0.0;
			autoCorrelationimagPart[i][j] = 0.0;
            for (int k = 0; k < cols; ++k) {
                autoCorrelationrealPart[i][j] += realPart[i][k]*realPart[j][k]+imagPart[i][k]*imagPart[j][k] ;
                autoCorrelationimagPart[i][j] += -realPart[i][k]*imagPart[j][k]+realPart[j][k]*imagPart[i][k];
            }
        }
    }
	std::cout << "AutoCorrelation Matrix:" << std::endl;
    for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < rows; ++j) {
            std::cout << autoCorrelationrealPart[i][j] << "+";
			std::cout << autoCorrelationimagPart[i][j] << "i\t";
        }
        std::cout << std::endl;
    }
	//printf("cccc\n");
	int count  = 0 ;
	int count1 = 0 ;
	float input = 1.0;
	long long longlongRepr = binaryStringToLongLong(floatToBinary(input));
	while (!top->io_dOut_last) 
	{
		int j = count%cols;
		int i = count/cols;
		float real = realPart[i][j];
		long long realin = binaryStringToLongLong(floatToBinary(real));
		float imag = imagPart[i][j];
		//if(count < (rows*cols)) cout << i << " "<< j <<" "<<real<<" "<<imag<<endl;
		long long imagin = binaryStringToLongLong(floatToBinary(imag));
		top->io_dIn_data = ((imagin)<<32)| realin;

		clockntimes(1);
		if(count < (rows*cols -1)) count ++;
	}
	clockntimes(100);
	top->io_conFig_valid = 1;
	M = 8;
	N = 20;
	top->io_conFig_data  = (N<<32)| M;
	clockntimes(1);
	int rows1 = M;
	int cols1 = N;
	float realPart1[rows1][cols1];
	float imagPart1[rows1][cols1];
	float autoCorrelationrealPart1[rows1][rows1];
	float autoCorrelationimagPart1[rows1][rows1];
	top->io_conFig_valid = 0;
	top->io_dIn_valid  = 1 ;
	top->io_dOut_ready = 1;
	for (int i = 0; i < rows1; ++i) {
        for (int j = 0; j < cols1; ++j) {
            realPart1[i][j] = dist(gen);//(float)(i%10);
            imagPart1[i][j] = dist(gen);//(float)(j%10);
        }
    }
	std::cout << "Matrix:"<<std::endl;
	for (int i = 0; i < rows1; ++i) {
        for (int j = 0; j < cols1; ++j) {
            std::cout << realPart1[i][j] << "+";
			std::cout << imagPart1[i][j] << "i\t";
        }
        std::cout << std::endl;
    }
	for (int i = 0; i < rows1; ++i) {
        for (int j = 0; j < rows1; ++j) {
            autoCorrelationrealPart1[i][j] = 0.0;
			autoCorrelationimagPart1[i][j] = 0.0;
            for (int k = 0; k < cols1; ++k) {
                autoCorrelationrealPart1[i][j] += realPart1[i][k]*realPart1[j][k]+imagPart1[i][k]*imagPart1[j][k] ;
                autoCorrelationimagPart1[i][j] += -realPart1[i][k]*imagPart1[j][k]+realPart1[j][k]*imagPart1[i][k];
            }
        }
    }
	std::cout << "AutoCorrelation Matrix:" << std::endl;
    for (int i = 0; i < rows1; ++i) {
        for (int j = 0; j < rows1; ++j) {
            std::cout << autoCorrelationrealPart1[i][j] << "+";
			std::cout << autoCorrelationimagPart1[i][j] << "i\t";
        }
        std::cout << std::endl;
    }
	//printf("cccc\n");
	count  = 0 ;
	count1 = 0 ;
	input = 1.0;
	//long long longlongRepr = binaryStringToLongLong(floatToBinary(input));
	while (!top->io_dOut_last) 
	{
		int j = count%cols1;
		int i = count/cols1;
		float real = realPart1[i][j];
		long long realin = binaryStringToLongLong(floatToBinary(real));
		float imag = imagPart1[i][j];
		//if(count < (rows*cols)) cout << i << " "<< j <<" "<<real<<" "<<imag<<endl;
		long long imagin = binaryStringToLongLong(floatToBinary(imag));
		top->io_dIn_data = ((imagin)<<32)| realin;

		clockntimes(1);
		if(count < (rows1*cols1 -1)) count ++;
	}
	auto end = std::chrono::high_resolution_clock::now();
    std::chrono::duration<double> duration = end - start;
    std::cout << "Time taken: " << duration.count() << " seconds" << std::endl;
	printf("wave:%d time:%f w/t:%f\n",wavecount,duration.count(), (double)wavecount/duration.count());
		//top->io_nextready = 1; 
		//clockntimes(10);
		//top->io_nextready = 0;
	//}
  
	//sim_exit();
}
#elif TESTMODULE == 12
void toptest(){
	//printf("hhhh\n");
	
	//for (int i = 0; i < 11 ; i ++) {
	auto start = std::chrono::high_resolution_clock::now();

    // 执行需要计时的代码

    reset(10);
	//printf("aaaa\n");
	top->io_conFig_valid = 1;
	top->io_conFig_data  = ((long long)(6)<<36)| (long long)(16);
	clockntimes(1);
	top->io_conFig_valid = 0;
	top->io_ddr_arready = 1;
	clockntimes(1);
	//确定系数
	top->io_ddr_rvalid = 1;
	//Lcount
	top->io_ddr_rdata = 0;
	clockntimes(1);
	//M 脉冲
	top->io_ddr_rdata = 5;
	clockntimes(1);
	//N 距离门
	top->io_ddr_rdata = 10;
	clockntimes(1);
	//K 通道
	top->io_ddr_rdata = 10;
	clockntimes(1);
	//ChannelShiftAddr
	top->io_ddr_rdata = 1000;
	clockntimes(1);
	//PulseShiftAddr
	top->io_ddr_rdata = 10;
	top->io_ddr_rlast =1;
	clockntimes(1);
	top->io_ddr_rlast = 0;
	//printf("cccc\n");
	int count  = 1 ;
	top->io_dout_ready = 1;
	while (!top->io_finish) 
	{
		top->io_ddr_rdata = 32;//(((long long)(count))<<32)| (long long)((count));
		if(count% 10 == 0) top->io_ddr_rlast = 1;
		else  top->io_ddr_rlast = 0;
		clockntimes(1);
		count ++;
	}
	clockntimes(10);
	top->io_conFig_valid = 1;
	top->io_conFig_data  = ((long long)(6)<<36)| (long long)(16);
	clockntimes(1);
	top->io_conFig_valid = 0;
	top->io_ddr_arready = 1;
	clockntimes(1);
	//确定系数
	top->io_ddr_rvalid = 1;
	//Lcount
	top->io_ddr_rdata = 0;
	clockntimes(1);
	//M 脉冲
	top->io_ddr_rdata = 8;
	clockntimes(1);
	//N 距离门
	top->io_ddr_rdata = 5;
	clockntimes(1);
	//K 通道
	top->io_ddr_rdata = 5;
	clockntimes(1);
	//ChannelShiftAddr
	top->io_ddr_rdata = 2000;
	clockntimes(1);
	//PulseShiftAddr
	top->io_ddr_rdata = 20;
	top->io_ddr_rlast =1;
	clockntimes(1);
	top->io_ddr_rlast = 0;
	//printf("cccc\n");
	count  = 1 ;
	top->io_dout_ready = 1;
	while (!top->io_finish) 
	{
		top->io_ddr_rdata = 64;//(((long long)(count))<<32)| (long long)((count));
		if(count% 5 == 0) top->io_ddr_rlast = 1;
		else  top->io_ddr_rlast = 0;
		clockntimes(1);
		count ++;
	}
	auto end = std::chrono::high_resolution_clock::now();
    std::chrono::duration<double> duration = end - start;
    std::cout << "Time taken: " << duration.count() << " seconds" << std::endl;
	printf("wave:%d time:%f w/t:%f\n",wavecount,duration.count(), (double)wavecount/duration.count());
		//top->io_nextready = 1; 
		//clockntimes(10);
		//top->io_nextready = 0;
	//}
  
	//sim_exit();
}


#endif 

#endif
