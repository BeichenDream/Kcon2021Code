gcc -fPIC -D_REENTRANT -I$JAVA_HOME/include/ -I$JAVA_HOME/include/linux -m64 -O3 -o MethodStubX64.o -c MethodStub.cpp
gcc -I$JAVA_HOME/include/ -I$JAVA_HOME/include/linux -no-pie -fno-pic -m32  -O3  -o MethodStubX32.o -c MethodStub.cpp
$JAVA_HOME/bin/javac GenerateMethodStub.java&&$JAVA_HOME/bin/java GenerateMethodStub