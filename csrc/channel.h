#ifndef CHANNEL_H
#define CHANNEL_H  
/*************************************************************************
	> File Name: channel.h
	> Author: yangli
	> Mail: 577647772@qq.com 
	> Created Time: 2023年05月21日 星期日 14时26分51秒
 ************************************************************************/
#include "common.h"
#include<iostream> 
#include <bitset>
#include <sstream>
using namespace std;

static default_random_engine channel(static_cast<unsigned>(time(NULL)));
std::string floatToBinary(float f) {
    // 将浮点数的内存表示转换为无符号整数
    unsigned int binaryRepresentation;
    std::memcpy(&binaryRepresentation, &f, sizeof(f));

    // 使用bitset将整数转换为二进制字符串
    return std::bitset<sizeof(f) * 8>(binaryRepresentation).to_string();
}
std::string floatToHex(float f) {
    // 将浮点数的内存表示转换为无符号整数
    unsigned int binaryRepresentation;
    std::memcpy(&binaryRepresentation, &f, sizeof(f));

    // 使用std::hex输出十六进制表示
    stringstream ss;
    ss << std::hex << binaryRepresentation;
    return ss.str();
}
long long binaryStringToLongLong(const std::string& binaryString) {
    // 使用 std::bitset 将二进制字符串转换为 long long
    return std::bitset<64>(binaryString).to_ullong();
}
#endif 
