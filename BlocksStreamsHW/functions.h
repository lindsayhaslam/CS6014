//
// Created by Lindsay Haslam on 2/22/24.
//
#include <iostream>
#include <array>
#include <algorithm>

#ifndef BLOCKSSTREAMSHW_FUNCTIONS_H
#define BLOCKSSTREAMSHW_FUNCTIONS_H
#define MAX_TABLE_SIZE 256

using namespace std;
using Key = array<uint8_t, 8>;
using SubsTable = array<uint8_t, 256>;
using Message = array<uint8_t, 8>;
using AllSubstitutionTables = array<SubsTable, 8>;
using namespace std;

Key generateKey(const std::string& password);
void shuffleSubsTable(SubsTable& array);
void rotateLeft(Message& array);
void rotateRight(Message& array);
void fillArray(SubsTable& table);



#endif //BLOCKSSTREAMSHW_FUNCTIONS_H
