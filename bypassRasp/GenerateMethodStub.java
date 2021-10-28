import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class GenerateMethodStub {
    static final int STT_FUNC = 2;
    static final int STT_GNU_IFUNC = 10;
    static byte[] elfHeader = {
            0x7F, 0x45, 0x4C, 0x46
    };

    static String x32AddressStub="aaaaaaaa";
    static String x64AddressStub="aaaaaaaaaaaaaaaa";


    public static HashMap readAllFunctionX64(File lib){
        HashMap functionMap=new HashMap();
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(lib, "r");
            byte[] magicNumberAndOtherInfo = new byte[16];
            randomAccessFile.read(magicNumberAndOtherInfo);
            if (Arrays.equals(elfHeader,Arrays.copyOfRange(magicNumberAndOtherInfo,0,4))){
                short objectType = Short.reverseBytes(randomAccessFile.readShort());
                short architecture = Short.reverseBytes(randomAccessFile.readShort());
                int objectFileVersion = Integer.reverseBytes(randomAccessFile.readInt());
                long entryPointVirtualAddress = Long.reverseBytes(randomAccessFile.readLong());
                long programHeaderTableFileOffset = Long.reverseBytes(randomAccessFile.readLong());
                long sectionHeaderTableFileOffset = Long.reverseBytes(randomAccessFile.readLong());
                int processorspecificFlags = Integer.reverseBytes(randomAccessFile.readInt());
                short elfHeaderSize = Short.reverseBytes(randomAccessFile.readShort());
                short programHeaderTableEntrySize = Short.reverseBytes(randomAccessFile.readShort());
                short programHeaderTableEntryCount = Short.reverseBytes(randomAccessFile.readShort());
                short sectionHeaderTableEntrySize = Short.reverseBytes(randomAccessFile.readShort());
                short sectionHeaderTableEntryCount = Short.reverseBytes(randomAccessFile.readShort());
                short sectionHeaderStringTableIndex = Short.reverseBytes(randomAccessFile.readShort());

                int codeAddress = 0x40;

                randomAccessFile.seek(sectionHeaderTableFileOffset+(sectionHeaderStringTableIndex*sectionHeaderTableEntrySize)+(
                        4 //sectionNameOffset
                                +4 //sectionType
                                +8 //sectionFlags
                                +8 //sectionAddress
                ));// shstrtab offset

                long shstrtab = Long.reverseBytes(randomAccessFile.readLong());
                long dynsymAddress = 0;
                long dynsymSize = 0;
                long dynsymEntsize = 0;
                long dynstrAddress= 0;

                for (short i = 0; i < sectionHeaderTableEntryCount; i++) {
                    randomAccessFile.seek(sectionHeaderTableFileOffset+i*sectionHeaderTableEntrySize);
                    int sectionNameOffset=Integer.reverseBytes(randomAccessFile.readInt());
                    int sectionType=Integer.reverseBytes(randomAccessFile.readInt());
                    long sectionFlags=Long.reverseBytes(randomAccessFile.readLong());
                    long sectionAddress=Long.reverseBytes(randomAccessFile.readLong());
                    long sectionOffset=Long.reverseBytes(randomAccessFile.readLong());
                    long sectionSize=Long.reverseBytes(randomAccessFile.readLong());
                    int sectionLink=Integer.reverseBytes(randomAccessFile.readInt());
                    int sectionInfo=Integer.reverseBytes(randomAccessFile.readInt());
                    long sectionAddralign=Long.reverseBytes(randomAccessFile.readLong());
                    long sectionEntsize=Long.reverseBytes(randomAccessFile.readLong());
                    if (sectionNameOffset!=0){
                        randomAccessFile.seek(sectionNameOffset+shstrtab);
                        String sectionName = readCString(randomAccessFile);
                        if (".symtab".equals(sectionName)){
                            dynsymAddress = sectionAddress==0?sectionOffset:sectionAddress;
                            dynsymSize = sectionSize;
                            dynsymEntsize = sectionEntsize;
                        }else if (".strtab".equals(sectionName)){
                            dynstrAddress = sectionAddress==0?sectionOffset:sectionAddress;
                        }
                    }

                }
                if (dynsymAddress!=0 && dynstrAddress!=0 && dynsymSize>0 &&dynsymEntsize>0){
                    long dynamicSymbolTableCount = dynsymSize/dynsymEntsize;
                    for (long i = 0; i < dynamicSymbolTableCount; i++) {
                        randomAccessFile.seek(dynsymAddress+dynsymEntsize*i);
                        int dynamicSymbolNameOffset = Integer.reverseBytes(randomAccessFile.readInt());
                        byte dynamicSymbolInfo = randomAccessFile.readByte();
                        byte dynamicSymbolOther = randomAccessFile.readByte();
                        short dynamicSymbolShndx = randomAccessFile.readShort();
                        long dynamicSymbolAddress = Long.reverseBytes(randomAccessFile.readLong());
                        long dynamicSymbolSize = Long.reverseBytes(randomAccessFile.readLong());
                        if (dynamicSymbolNameOffset!=0 && ((dynamicSymbolInfo&0xf)==STT_FUNC||(dynamicSymbolInfo&0xf)==STT_GNU_IFUNC)){
                            randomAccessFile.seek(dynstrAddress+dynamicSymbolNameOffset);
                            String dynamicSymbolName = readCString(randomAccessFile);
                            byte[] funtcionStub = new byte[(int) dynamicSymbolSize];
                            randomAccessFile.seek(codeAddress+dynamicSymbolAddress);
                            randomAccessFile.readFully(funtcionStub);
                            functionMap.put(dynamicSymbolName,funtcionStub);
                        }
                    }
                }
            }
        }catch (Exception e){

        }
        return functionMap;
    }
    public static HashMap readAllFunctionX32(File lib){
        HashMap functionMap=new HashMap();
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(lib, "r");
            byte[] magicNumberAndOtherInfo = new byte[16];
            randomAccessFile.read(magicNumberAndOtherInfo);
            if (Arrays.equals(elfHeader,Arrays.copyOfRange(magicNumberAndOtherInfo,0,4))){
                short objectType = Short.reverseBytes(randomAccessFile.readShort());
                short architecture = Short.reverseBytes(randomAccessFile.readShort());
                int objectFileVersion = Integer.reverseBytes(randomAccessFile.readInt());
                int entryPointVirtualAddress = Integer.reverseBytes(randomAccessFile.readInt());
                int programHeaderTableFileOffset = Integer.reverseBytes(randomAccessFile.readInt());
                int sectionHeaderTableFileOffset = Integer.reverseBytes(randomAccessFile.readInt());
                int processorspecificFlags = Integer.reverseBytes(randomAccessFile.readInt());
                short elfHeaderSize = Short.reverseBytes(randomAccessFile.readShort());
                short programHeaderTableEntrySize = Short.reverseBytes(randomAccessFile.readShort());
                short programHeaderTableEntryCount = Short.reverseBytes(randomAccessFile.readShort());
                short sectionHeaderTableEntrySize = Short.reverseBytes(randomAccessFile.readShort());
                short sectionHeaderTableEntryCount = Short.reverseBytes(randomAccessFile.readShort());
                short sectionHeaderStringTableIndex = Short.reverseBytes(randomAccessFile.readShort());

                int codeAddress = 0x40;

                randomAccessFile.seek(sectionHeaderTableFileOffset+(sectionHeaderStringTableIndex*sectionHeaderTableEntrySize)+(
                        4 //sectionNameOffset
                                +4 //sectionType
                                +4 //sectionFlags
                                +4 //sectionAddress
                ));// shstrtab offset

                int shstrtab = Integer.reverseBytes(randomAccessFile.readInt());
                int dynsymAddress = 0;
                int dynsymSize = 0;
                int dynsymEntsize = 0;
                int dynstrAddress= 0;

                for (short i = 0; i < sectionHeaderTableEntryCount; i++) {
                    randomAccessFile.seek(sectionHeaderTableFileOffset+i*sectionHeaderTableEntrySize);
                    int sectionNameOffset=Integer.reverseBytes(randomAccessFile.readInt());
                    int sectionType=Integer.reverseBytes(randomAccessFile.readInt());
                    int sectionFlags=Integer.reverseBytes(randomAccessFile.readInt());
                    int sectionAddress=Integer.reverseBytes(randomAccessFile.readInt());
                    int sectionOffset=Integer.reverseBytes(randomAccessFile.readInt());
                    int sectionSize=Integer.reverseBytes(randomAccessFile.readInt());
                    int sectionLink=Integer.reverseBytes(randomAccessFile.readInt());
                    int sectionInfo=Integer.reverseBytes(randomAccessFile.readInt());
                    int sectionAddralign=Integer.reverseBytes(randomAccessFile.readInt());
                    int sectionEntsize=Integer.reverseBytes(randomAccessFile.readInt());
                    if (sectionNameOffset!=0){
                        randomAccessFile.seek(sectionNameOffset+shstrtab);
                        String sectionName = readCString(randomAccessFile);
                        if (".symtab".equals(sectionName)){
                            dynsymAddress =sectionAddress==0?sectionOffset:sectionAddress;
                            dynsymSize = sectionSize;
                            dynsymEntsize = sectionEntsize;
                        }else if (".strtab".equals(sectionName)){
                            dynstrAddress = sectionAddress==0?sectionOffset:sectionAddress;
                        }
                    }

                }
                if (dynsymAddress!=0 && dynstrAddress!=0 && dynsymSize>0 &&dynsymEntsize>0){
                    long dynamicSymbolTableCount = dynsymSize/dynsymEntsize;
                    for (long i = 0; i < dynamicSymbolTableCount; i++) {
                        randomAccessFile.seek(dynsymAddress+dynsymEntsize*i);
                        int dynamicSymbolNameOffset = Integer.reverseBytes(randomAccessFile.readInt());
                        int dynamicSymbolAddress = Integer.reverseBytes(randomAccessFile.readInt());
                        int dynamicSymbolSize = Integer.reverseBytes(randomAccessFile.readInt());
                        byte dynamicSymbolInfo = randomAccessFile.readByte();
                        byte dynamicSymbolOther = randomAccessFile.readByte();
                        short dynamicSymbolShndx = randomAccessFile.readShort();

                        if (dynamicSymbolNameOffset!=0 && ((dynamicSymbolInfo&0xf)==STT_FUNC||(dynamicSymbolInfo&0xf)==STT_GNU_IFUNC)){
                            randomAccessFile.seek(dynstrAddress+dynamicSymbolNameOffset);
                            String dynamicSymbolName = readCString(randomAccessFile);
                            byte[] funtcionStub = new byte[(int) dynamicSymbolSize];
                            randomAccessFile.seek(codeAddress+dynamicSymbolAddress);
                            randomAccessFile.readFully(funtcionStub);
                            functionMap.put(dynamicSymbolName,funtcionStub);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return functionMap;
    }
    public static String readCString(DataInput inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte c;
        while ((c= (byte) inputStream.readByte())!=0){
            buffer.write(c);
        }
        return new String(buffer.toByteArray());
    }

    public static String byteArrayToHex(byte[] bytes) {
        String strHex = "";
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex);
        }
        return sb.toString().trim();
    }

    public static byte[] hexToByte(String hex) {
        int m = 0, n = 0;
        int byteLen = hex.length() / 2;
        byte[] ret = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
            ret[i] = Byte.valueOf((byte) intVal);
        }
        return ret;
    }

    public static void main(String[] args) {
        String[] functions={"Java_Hack_mmap","Java_Hack_inject","Java_Hack_jmp","Java_Hack_JvmTi","Java_Hack_patchVM"};
        String[] functionRemark={"mmap","transform","jmp","jvmti","patchVM"};

        HashMap functionMap = readAllFunctionX64(new File("MethodStubX64.o"));
        Iterator names = functionMap.keySet().iterator();
        while (names.hasNext()){
            String functionName=names.next().toString();
            for (int i = 0; i < functions.length; i++) {
                String funName = functions[i];
                if (functionName.contains(funName)){
                    String functionHex=byteArrayToHex((byte[]) functionMap.get(functionName));
                    int addressIndex = functionHex.indexOf(x64AddressStub);
                    if (addressIndex!=-1){
                        String left=functionHex.substring(0,addressIndex);
                        String right=functionHex.substring(addressIndex+x64AddressStub.length());
                        functionHex=left+"%s"+right;
                        System.out.println(String.format("private static String %sHex64 = \"%s\";",functionRemark[i],functionHex));
                        break;
                    }else {
                        System.out.println(String.format("private static String %sHex64 = \"%s\";",functionRemark[i],functionHex));
                    }
                }
            }
        }

        functionMap = readAllFunctionX32(new File("MethodStubX32.o"));
        names = functionMap.keySet().iterator();
        while (names.hasNext()){
            String functionName=names.next().toString();
            for (int i = 0; i < functions.length; i++) {
                String funName = functions[i];
                if (functionName.contains(funName)){
                    String functionHex=byteArrayToHex((byte[]) functionMap.get(functionName));
                    int addressIndex = functionHex.indexOf(x32AddressStub);
                    if (addressIndex!=-1){
                        String left=functionHex.substring(0,addressIndex);
                        String right=functionHex.substring(addressIndex+x32AddressStub.length());
                        functionHex=left+"%s"+right;
                        System.out.println(String.format("private static String %sHex32 = \"%s\";",functionRemark[i],functionHex));
                        break;
                    }else {
                        System.out.println(String.format("private static String %sHex32 = \"%s\";",functionRemark[i],functionHex));
                    }
                }
            }
        }

    }
}
