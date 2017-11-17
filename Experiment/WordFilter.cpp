#include<iostream>
#include<fstream>
#include<string>
using namespace std;

bool hasSingleQ(char str[]){
    int len=strlen(str);
    for(int i=0;i<len-1;i++){
        if(str[i]=='q'&&str[i+1]!='u'){
            return true;
        }
    }
    return false||str[len-1]=='q';
}

int main(){
    fstream ifs("words copy.txt",fstream::in);
    fstream ofs("filtered words.txt",fstream::out);

    int a=0,b=0,c=0;
    char str[128];
    while(!ifs.getline(str,128).eof()){
        a++;
        if(3<=strlen(str)&&strlen(str)<=16&&(!hasSingleQ(str))){
        //if(3<=strlen(str)&&strlen(str)<=16){
            b++;
            ofs<<str<<endl;
            c+=strlen(str);
        }
    }
    cout<<a<<" "<<b<<" "<<c<<endl;
}