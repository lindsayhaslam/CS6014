//
// Created by Lindsay Haslam on 2/22/24.
//

#include "functions.h"
#include <iostream>
#include <array>
#include <algorithm>
using namespace std;

void fillArray(SubsTable& table){
    for (size_t i = 0; i < table.size(); i++){
        table[i] = static_cast<uint8_t>(i);
    }
}
//Build a function that generate a key based on the password
Key generateKey(const std::string& password){
    //Create a key that is 8 bytes, filled with zeros
    Key key = {0,0,0,0,0,0,0,0};
    //Iterate over each char
    for (int i = 0; i < password.length(); i++){
        key[i % 8] ^= static_cast<uint8_t>(password[i]);
    }
    return key;
}
//Helper function to randomly shuffle the byte table according to the Fisher-Yates algorithm
void shuffleSubsTable(SubsTable& array) {
    //Loop through the table in the order
    for (int i = MAX_TABLE_SIZE -1; i > 0; i--){
        int j = rand() % (i + 1);
        std::swap(array[i],array[j]);
    }
}
void rotateLeft(Message& array){
    uint8_t firstBit = (array[0] & 0x80) >> 7;
    for (size_t i = 0; i < array.size() - 1; ++i){
        array[i] = (array[i] << 1) | ((array[i+1] & 0x80) >> 7);
    }
    array[array.size() -1] = (array[array.size() -1] << 1) | firstBit;
}

void rotateRight(Message& array){
    uint8_t lastBit = (array[array.size() -1] & 0x01) << 7;
    for (size_t i = array.size() - 1; i > 0; --i){
        array[i] = (array[i] >> 1) | ((array[i -1] & 0x01) << 7);
    }
    array[0] = (array[0] >> 1) | lastBit;
}

