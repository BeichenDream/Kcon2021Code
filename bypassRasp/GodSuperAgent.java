import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class GodSuperAgent {
    private static final int STT_FUNC = 2;
    private static final int STT_GNU_IFUNC = 10;
    private static short GetEnvironmentLocalStorageIndex = 147;
    private static byte[] elfHeader = {
            0x7F, 0x45, 0x4C, 0x46
    };
    private static String transformHex64 = "4157415641554989fd415455534881ec98000000488b0748c744242800000000488d74242848c744243000000000c74424240000000048c744243800000000ff90d806000085c07519488b7c2428488d742430ba00010130488b07ff503085c0741e31c04881c4980000005b5d415c415d415e415fc3662e0f1f840000000000488b7c2430488d6c24604889ee488b07ff90c002000085c075c8488b7c2430814c2460000220004889ee488b07ff906804000085c075ab488b7c2430488d542438488d742424488b07ff906802000085c0758f8b74242485f674874531ff4c8d642448488d6c244049be9aaaaa8a9aaaaa8aeb160f1f400048635424244983c7014c39fa0f8616010000488b442438488b7c24304c89e14889ea48c74424400000000048c7442448000000004a8b1cf8488b074889deff907801000085c075b8488b4c24400fb611413a1675ab4c89f6488d41014829ceeb1c0f1f800000000048894424400fb60c064883c0010fb650ff38ca758384d275e748b87472616e73666f72488d4c24704889de4c89ef48ba616e672f436c61734889442456b86d000000668944245e48b8284c6a6176612f6c4889442470498b45004889542478ba420000006689942484000000488d542456c7842480000000733b295bff908803000048894424104885c00f856b010000498b45004c89efff902007000084c00f84f3feffff498b45004c89efff9088000000e9e1feffff660f1f84000000000085d20f843afeffffc644240f0031db48c744241000000000488d44245631ed48894424180f1f4000488b442438807c240f004c8b24e80f84c80000004d8b4d00488b5424104c89e14889de4c89ef31c041ff91900300004989c74885c00f8483000000498b4500488b5424184c89fe4c89efc644245600ff90c00500004c89fe4c89ef4989c6498b4500ff9058050000488b7c2430488d542470be0100000089442478488b07c744247c000000004c89b424800000004c89642470ff90b0020000498b45004c89fe4c89ef31c94c89f2ff9000060000498b45004c89fe4c89efff90b8000000498b45004c89efff902007000084c0740d498b45004c89efff9088000000498b45004c89e64c89ef4883c501ff90b800000048634424244839e80f8702ffffff807c240f00b86c1e00000f840cfdffffe909fdffff8b4c24244885db0f9544240f85c90f85c9feffffebd5";
    private static String patchVMHex64 = "4154660fefc031c9ba36000000554889fd31f631c04883ec184c8b070f1104244989e441ff5008488b45004c89e64889efff90c0020000488b45004c89e64889ef6681642403fb9fff90680400004883c418b8640000005d415cc3";
    private static String jvmtiHex64 = "55660fefc04883ec20488b0748c70424000000004889e648c7442408000000000f11442410ff90d806000085c07518488b3c24488d742408ba00010130488b07ff503085c0740931c04883c4205dc390488b7c2408488d6c24104889ee488b07ff90c002000085c075dd488b7c2408814c2410000220004889ee488b07ff906804000085c075c0488b442408ebbb";
    private static String mmapHex64 = "4531c941b8ffffffffb92200000031ffba07000000be0000100048b8%sffe0";
    private static String jmpHex64 = "48b8%sffe0";
    private static String transformHex32 = "5557565383ec748b9c24880000008d54241cc744241c00000000c744242000000000c7442424000000008b03c7442428000000005253ff906c03000083c41085c0751e8b44241483ec048b1068000101308d4c24205150ff521883c41085c0740f31c031d283c46c5b5e5f5dc38d76008b44241883ec088d7424408b105650ff926001000083c41085c075d58b442418814c24380002200083ec088b105650ff923402000083c41085c075b58b44241883ec048d4c24248b10518d4c24245150ff92340100008944241483c41085c075908b7c241c85ff7488899c248000000031ff8d742428eb178db42600000000908b44241c83c70139f80f86290100008b442420c744242400000000c7442428000000008b1cb88b4424188b10568d4c2428515350ff92bc00000083c41085c075bf8b4c24240fb6113a159aaaaa8a75b0bd9aaaaa8a8d410129cdeb188d742600894424240fb64c050083c0010fb650ff38ca758c84d275e8b86d000000ba42000000c744242e7472616e66894424368b842480000000668954245c8d542448c744243273666f728b00c7442448284c6a61c744244c76612f6cc7442450616e672fc7442454436c6173c7442458733b295b528d5424325253ffb4248c000000ff90c401000083c41085c00f855a01000083ec0c8b84248c0000008b00ffb4248c000000ff909003000083c41084c00f84ecfeffff83ec0c8b84248c0000008b00ffb4248c000000ff504483c410e9cefeffff8db6000000008b9c248000000085c00f842afeffffc64424030031edc744240800000000896c240c8b74240466908b442420807c2403008b3cb00f84a70000008b0357ff742410ff74241053ff90c801000089c583c41085c0746dc644242e0083ec048b038d4c2432515553ff90e00200008944241459588b035553ff90ac0200008b4c24148944245c8b442428894c2460897c245883c40c8b088d54244c526a0150ff91580100008b0383c4106a00ff7424085553ff9000030000588b035a5553ff505c83c41083ec0c8b0353ff909003000083c41084c0740c83ec0c8b0353ff504483c41083ec088b0383c6015753ff505c83c4103974241c0f872dffffff31d2807c240300b86c1e00000f842cfdffffe92bfdffff895c24088b4c240889c58b74241c8b9c248000000085c90f9544240385f60f85e8feffffebc3";
    private static String patchVMHex32 = "565383ec148b5c2420c7042400000000c744240400000000c7442408000000008b03c744240c000000006a006a366a0053ff5004588b035a8d7424085653ff90600100008b036681642413fb9f89f483ec085653ff903402000089f4b86400000083c4145b5ec3";
    private static String jvmtiHex32 = "5383ec308b4424388d4c2410c744241000000000c744241400000000c7442418000000008b10c744241c00000000c744242000000000c7442424000000005150ff926c03000083c41085c0751e8b44240883ec048b1068000101308d4c24145150ff521883c41085c0740d31c031d283c4285bc38d7426008b44240c83ec088d5c24188b105350ff926001000083c41085c075d78b44240c814c24100002200083ec088b105350ff923402000083c41085c075b78b44240c99ebb4";
    private static String mmapHex32 = "83ec14b8%s6a006aff6a226a0768000010006a00ffd09983c42cc3";
    private static String jmpHex32 = "83ec18b8%sff74241cffd09983c41cc3";





    public static long findFunctionAddressX64(File lib, String functionName){
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(lib, "r");
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
                        if (".dynsym".equals(sectionName)){
                            dynsymAddress =sectionAddress;
                            dynsymSize = sectionSize;
                            dynsymEntsize = sectionEntsize;
                        }else if (".dynstr".equals(sectionName)){
                            dynstrAddress = sectionAddress;
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
                        if (dynamicSymbolNameOffset!=0 &&dynamicSymbolAddress!=0 && ((dynamicSymbolInfo&0xf)==STT_FUNC||(dynamicSymbolInfo&0xf)==STT_GNU_IFUNC)){
                            randomAccessFile.seek(dynstrAddress+dynamicSymbolNameOffset);
                            String dynamicSymbolName = readCString(randomAccessFile);
                            if (functionName.equals(dynamicSymbolName)){
                                if (randomAccessFile!=null){
                                    try {
                                        randomAccessFile.close();
                                    }catch (Throwable e){

                                    }
                                }
                                return dynamicSymbolAddress;
                            }
                        }
                    }
                }
            }
        }catch (Exception e){

        }
        if (randomAccessFile!=null){
            try {
                randomAccessFile.close();
            }catch (Throwable e){

            }
        }
        return 0;
    }
    public static long findFunctionAddressX32(File lib, String functionName){
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(lib, "r");
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
                        if (".dynsym".equals(sectionName)){
                            dynsymAddress =sectionAddress;
                            dynsymSize = sectionSize;
                            dynsymEntsize = sectionEntsize;
                        }else if (".dynstr".equals(sectionName)){
                            dynstrAddress = sectionAddress;
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

                        if (dynamicSymbolNameOffset!=0 &&dynamicSymbolAddress!=0 && ((dynamicSymbolInfo&0xf)==STT_FUNC||(dynamicSymbolInfo&0xf)==STT_GNU_IFUNC)){
                            randomAccessFile.seek(dynstrAddress+dynamicSymbolNameOffset);
                            String dynamicSymbolName = readCString(randomAccessFile);
                            if (functionName.equals(dynamicSymbolName)){
                                if (randomAccessFile!=null){
                                    try {
                                        randomAccessFile.close();
                                    }catch (Throwable e){

                                    }
                                }
                                return dynamicSymbolAddress;
                            }
                        }
                    }
                }
            }
        }catch (Exception e){

        }
        if (randomAccessFile!=null){
            try {
                randomAccessFile.close();
            }catch (Throwable e){

            }
        }
        return 0;
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
    public static Map<String, Long> parseMemoryLibrary(){
        HashMap<String, Long> memoryLibrary = new HashMap();
        RandomAccessFile memRandomAccessFile = null;
        RandomAccessFile mapsRandomAccessFile = null;

        try {
            mapsRandomAccessFile = new RandomAccessFile("/proc/self/maps", "r");
            memRandomAccessFile = new RandomAccessFile("/proc/self/mem", "r");
            String line = null;
            while ((line = mapsRandomAccessFile.readLine())!=null){
                try {
                    StringTokenizer st = new StringTokenizer(line);
                    String[] addressArray=st.nextToken().split("-");
                    long startAddress = new BigInteger(addressArray[0], 16).longValue();
                    long endAddress = new BigInteger(addressArray[1], 16).longValue();
                    String permission = st.nextToken();
                    long size = Long.parseLong(st.nextToken());
                    String info = st.nextToken();
                    String handle = st.nextToken();
                    String libFile = st.nextToken();
                    if (startAddress!=0&&libFile!=null&&!libFile.isEmpty()&&endAddress-startAddress!=0){
                        if (startAddress<0){
                            startAddress = Long.parseLong(Long.toHexString(startAddress).substring(8),16);
                        }
                        memRandomAccessFile.seek(startAddress);
                        byte[] elfHeader = new byte[4];
                        memRandomAccessFile.readFully(elfHeader);
                        if (Arrays.equals(GodSuperAgent.elfHeader,elfHeader)){
                            memoryLibrary.put(libFile,startAddress);
                        }
                    }
                }catch (Exception e){

                }

            }

        }catch (Throwable e){
            //e.printStackTrace();
        }
        try {
            if (memRandomAccessFile!=null){
                memRandomAccessFile.close();
            }
        }catch (Throwable e){

        }
        try {
            if (mapsRandomAccessFile!=null){
                mapsRandomAccessFile.close();
            }
        }catch (Throwable e){

        }
        return memoryLibrary;
    }

    public static boolean patchVM32(Map<String, Long> memoryLibraryMap){
        long Java_java_io_RandomAccessFile_length = 0;
        long JNI_GetCreatedJavaVMs = 0;
        long mmap = 0;
        Iterator libs = memoryLibraryMap.keySet().iterator();
        while (libs.hasNext()){
            String lib=libs.next().toString();
            long libAddress = memoryLibraryMap.get(lib);
            if (JNI_GetCreatedJavaVMs==0){
                JNI_GetCreatedJavaVMs=findFunctionAddressX32(new File(lib),"JNI_GetCreatedJavaVMs");
                if (JNI_GetCreatedJavaVMs!=0){
                    JNI_GetCreatedJavaVMs = JNI_GetCreatedJavaVMs+libAddress;
                }
            }
            if (Java_java_io_RandomAccessFile_length==0){
                Java_java_io_RandomAccessFile_length=findFunctionAddressX32(new File(lib),"Java_java_io_RandomAccessFile_length");
                if (Java_java_io_RandomAccessFile_length!=0){
                    Java_java_io_RandomAccessFile_length = Java_java_io_RandomAccessFile_length+libAddress;
                }
            }
            if (mmap==0){
                mmap=findFunctionAddressX32(new File(lib),"mmap");
                if (mmap!=0){
                    mmap = mmap+libAddress;
                }
            }
            if (JNI_GetCreatedJavaVMs!=0&&mmap!=0&&Java_java_io_RandomAccessFile_length!=0){
                break;
            }
        }
        if (JNI_GetCreatedJavaVMs!=0&&Java_java_io_RandomAccessFile_length!=0&&mmap!=0){
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile("/proc/self/mem","rw");
                int backSize = mmapHex32.length();
                if (jmpHex32.length()>backSize){
                    backSize=jmpHex32.length();
                }
                backSize=backSize+4;

                //backups
                byte[] backBuffer = new byte[backSize];
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.readFully(backBuffer);

                //mmap mem
                ByteBuffer byteBuffer=ByteBuffer.allocate(4);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putInt((int)mmap);

                byte[] buffer = hexToByte(String.format(mmapHex32,byteArrayToHex(byteBuffer.array())));
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.write(buffer);
                long newAddress = randomAccessFile.length();
                //check address
                if (newAddress<0){
                    newAddress=Long.parseLong(Long.toHexString(newAddress).substring(8),16);
                }

                //write payload
                byteBuffer=ByteBuffer.allocate(4);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putInt((int)JNI_GetCreatedJavaVMs);
                buffer = hexToByte(String.format(jvmtiHex32,byteArrayToHex(byteBuffer.array())));
                randomAccessFile.seek(newAddress);
                randomAccessFile.write(buffer);

                //jmp payload
                byteBuffer=ByteBuffer.allocate(4);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putInt((int)newAddress);

                buffer = hexToByte(String.format(jmpHex32,byteArrayToHex(byteBuffer.array())));
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.write(buffer);
                //get jvmtienv
                long jvmtiEnv  = randomAccessFile.length();//jvmtiEnv
                //recover
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.write(backBuffer);

                //patch VM....
                if (jvmtiEnv<0){
                    randomAccessFile.seek(Long.parseLong(Long.toHexString(jvmtiEnv).substring(8),16));
                }else {
                    randomAccessFile.seek(jvmtiEnv);
                }
                byteBuffer=ByteBuffer.allocate(4);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putInt(randomAccessFile.readInt());//jvmtiInterface_1_
                long functions = Long.parseLong(byteArrayToHex(byteBuffer.array()),16);
                randomAccessFile.seek(functions+((GetEnvironmentLocalStorageIndex-1)*4));
                byteBuffer=ByteBuffer.allocate(4);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putInt(randomAccessFile.readInt());
                randomAccessFile.seek(Long.parseLong(byteArrayToHex(byteBuffer.array()),16)); //real address
                randomAccessFile.write(hexToByte(patchVMHex32));

                return true;

            }catch (Throwable e){
                e.printStackTrace();
            }
            try{
                if (randomAccessFile!=null){
                    randomAccessFile.close();
                }
            }catch (Throwable e){

            }
        }
        return false;
    }
    public static boolean patchVM64(Map<String, Long> memoryLibraryMap){
        long Java_java_io_RandomAccessFile_length = 0;
        long JNI_GetCreatedJavaVMs = 0;
        long mmap = 0;
        Iterator libs = memoryLibraryMap.keySet().iterator();
        while (libs.hasNext()){
            String lib=libs.next().toString();
            long libAddress = memoryLibraryMap.get(lib);
            if (JNI_GetCreatedJavaVMs==0){
                JNI_GetCreatedJavaVMs=findFunctionAddressX64(new File(lib),"JNI_GetCreatedJavaVMs");
                if (JNI_GetCreatedJavaVMs!=0){
                    JNI_GetCreatedJavaVMs = JNI_GetCreatedJavaVMs+libAddress;
                }
            }
            if (Java_java_io_RandomAccessFile_length==0){
                Java_java_io_RandomAccessFile_length=findFunctionAddressX64(new File(lib),"Java_java_io_RandomAccessFile_length");
                if (Java_java_io_RandomAccessFile_length!=0){
                    Java_java_io_RandomAccessFile_length = Java_java_io_RandomAccessFile_length+libAddress;
                }
            }
            if (mmap==0){
                mmap=findFunctionAddressX64(new File(lib),"mmap");
                if (mmap!=0){
                    mmap = mmap+libAddress;
                }
            }
            if (JNI_GetCreatedJavaVMs!=0&&mmap!=0&&Java_java_io_RandomAccessFile_length!=0){
                break;
            }
        }
        if (JNI_GetCreatedJavaVMs!=0&&Java_java_io_RandomAccessFile_length!=0&&mmap!=0){
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile("/proc/self/mem","rw");
                int backSize = mmapHex64.length();
                if (jmpHex64.length()>backSize){
                    backSize=jmpHex64.length();
                }
                backSize=backSize+8;

                //backups
                byte[] backBuffer = new byte[backSize];
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.readFully(backBuffer);

                //mmap mem
                ByteBuffer byteBuffer=ByteBuffer.allocate(8);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putLong(mmap);

                byte[] buffer = hexToByte(String.format(mmapHex64,byteArrayToHex(byteBuffer.array())));
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.write(buffer);
                long newAddress = randomAccessFile.length();
                //check address
                if (newAddress<0){
                    newAddress=Long.parseLong(Long.toHexString(newAddress).substring(8),16);
                }

                //write payload
                byteBuffer=ByteBuffer.allocate(8);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putLong(JNI_GetCreatedJavaVMs);
                buffer = hexToByte(String.format(jvmtiHex64,byteArrayToHex(byteBuffer.array())));
                randomAccessFile.seek(newAddress);
                randomAccessFile.write(buffer);

                //jmp payload
                byteBuffer=ByteBuffer.allocate(8);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putLong(newAddress);

                buffer = hexToByte(String.format(jmpHex64,byteArrayToHex(byteBuffer.array())));
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.write(buffer);
                //get jvmtienv
                long jvmtiEnv  = randomAccessFile.length();//jvmtiEnv
                //recover
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.write(backBuffer);

                //patch VM....
                randomAccessFile.seek(jvmtiEnv);
                byteBuffer=ByteBuffer.allocate(8);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putLong(randomAccessFile.readLong());//jvmtiInterface_1_
                long functions = Long.parseLong(byteArrayToHex(byteBuffer.array()),16);
                randomAccessFile.seek(functions+((GetEnvironmentLocalStorageIndex-1)*8));
                byteBuffer=ByteBuffer.allocate(8);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putLong(randomAccessFile.readLong());
                randomAccessFile.seek(Long.parseLong(byteArrayToHex(byteBuffer.array()),16)); //real address
                randomAccessFile.write(hexToByte(patchVMHex64));

                return true;

            }catch (Throwable e){
                //e.printStackTrace();
            }
            try{
                if (randomAccessFile!=null){
                    randomAccessFile.close();
                }
            }catch (Throwable e){

            }
        }
        return false;
    }
    public static void run32(Map<String, Long> memoryLibraryMap){
        long Java_java_io_RandomAccessFile_length = 0;
        long mmap = 0;
        Iterator libs = memoryLibraryMap.keySet().iterator();
        while (libs.hasNext()){
            String lib=libs.next().toString();
            long libAddress = memoryLibraryMap.get(lib);
            if (Java_java_io_RandomAccessFile_length==0){
                Java_java_io_RandomAccessFile_length=findFunctionAddressX32(new File(lib),"Java_java_io_RandomAccessFile_length");
                if (Java_java_io_RandomAccessFile_length!=0){
                    Java_java_io_RandomAccessFile_length = Java_java_io_RandomAccessFile_length+libAddress;
                }
            }
            if (mmap==0){
                mmap=findFunctionAddressX32(new File(lib),"mmap");
                if (mmap!=0){
                    mmap = mmap+libAddress;
                }
            }
            if (mmap!=0&&Java_java_io_RandomAccessFile_length!=0){
                break;
            }
        }
        if (mmap!=0&&Java_java_io_RandomAccessFile_length!=0){
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile("/proc/self/mem","rw");
                int backSize = mmapHex32.length();
                if (jmpHex32.length()>backSize){
                    backSize=jmpHex32.length();
                }
                backSize=backSize+4;
                //backups
                byte[] backBuffer = new byte[backSize];
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.readFully(backBuffer);

                //mmap mem
                ByteBuffer byteBuffer=ByteBuffer.allocate(4);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putInt((int)mmap);

                byte[] buffer = hexToByte(String.format(mmapHex32,byteArrayToHex(byteBuffer.array())));
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.write(buffer);
                long newAddress = randomAccessFile.length();
                //check address
                if (newAddress<0){
                    newAddress=Long.parseLong(Long.toHexString(newAddress).substring(8),16);
                }

                //write payload

                buffer = hexToByte(transformHex32);

                int classNameAddress = (int)newAddress+buffer.length;
                byteBuffer=ByteBuffer.allocate(4);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putInt(classNameAddress);
                buffer = hexToByte(byteArrayToHex(buffer).replace("9aaaaa8a",byteArrayToHex(byteBuffer.array()))); //replace className Address

                randomAccessFile.seek(newAddress);
                randomAccessFile.write(buffer);
                randomAccessFile.write(String.format("L%s;",GodSuperAgent.class.getName().replace(".","/")).getBytes());
                randomAccessFile.write(0x00);
                //patch VM....
                patchVM32(memoryLibraryMap);
                //jmp payload
                byteBuffer=ByteBuffer.allocate(4);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putInt((int)newAddress);

                buffer = hexToByte(String.format(jmpHex32,byteArrayToHex(byteBuffer.array())));
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.write(buffer);
                if (randomAccessFile.length()==7788){
                    System.out.println("ok");
                }
                //recover
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.write(backBuffer);

            }catch (Throwable e){
                //e.printStackTrace();
            }
            try{
                if (randomAccessFile!=null){
                    randomAccessFile.close();
                }
            }catch (Throwable e){

            }
        }
    }
    public static void run64(Map<String, Long> memoryLibraryMap){
        long Java_java_io_RandomAccessFile_length = 0;
        long mmap = 0;
        Iterator libs = memoryLibraryMap.keySet().iterator();
        while (libs.hasNext()){
            String lib=libs.next().toString();
            long libAddress = memoryLibraryMap.get(lib);
            if (Java_java_io_RandomAccessFile_length==0){
                Java_java_io_RandomAccessFile_length=findFunctionAddressX64(new File(lib),"Java_java_io_RandomAccessFile_length");
                if (Java_java_io_RandomAccessFile_length!=0){
                    Java_java_io_RandomAccessFile_length = Java_java_io_RandomAccessFile_length+libAddress;
                }
            }
            if (mmap==0){
                mmap=findFunctionAddressX64(new File(lib),"mmap");
                if (mmap!=0){
                    mmap = mmap+libAddress;
                }
            }
            if (mmap!=0&&Java_java_io_RandomAccessFile_length!=0){
                break;
            }
        }
        if (mmap!=0&&Java_java_io_RandomAccessFile_length!=0){
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile("/proc/self/mem","rw");
                int backSize = mmapHex64.length();
                if (jmpHex64.length()>backSize){
                    backSize=jmpHex64.length();
                }
                backSize=backSize+8;
                //backups
                byte[] backBuffer = new byte[backSize];
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.readFully(backBuffer);

                //mmap mem
                ByteBuffer byteBuffer=ByteBuffer.allocate(8);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putLong(mmap);

                byte[] buffer = hexToByte(String.format(mmapHex64,byteArrayToHex(byteBuffer.array())));
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.write(buffer);
                long newAddress = randomAccessFile.length();
                //check address
                if (newAddress<0){
                    newAddress=Long.parseLong(Long.toHexString(newAddress).substring(8),16);
                }

                //write payload
                buffer = hexToByte(transformHex64);

                long classNameAddress = newAddress+buffer.length;
                byteBuffer=ByteBuffer.allocate(8);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putLong(classNameAddress);
                buffer = hexToByte(byteArrayToHex(buffer).replace("9aaaaa8a9aaaaa8a",byteArrayToHex(byteBuffer.array())));


                randomAccessFile.seek(newAddress);
                randomAccessFile.write(buffer);
                randomAccessFile.write(String.format("L%s;",GodSuperAgent.class.getName().replace(".","/")).getBytes());
                randomAccessFile.write(0x00);
                //patch VM....
                patchVM64(memoryLibraryMap);
                //jmp payload
                byteBuffer=ByteBuffer.allocate(8);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putLong(newAddress);

                buffer = hexToByte(String.format(jmpHex64,byteArrayToHex(byteBuffer.array())));
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.write(buffer);
                if (randomAccessFile.length()==7788){
                    System.out.println("ok");
                }
                //recover
                randomAccessFile.seek(Java_java_io_RandomAccessFile_length);
                randomAccessFile.write(backBuffer);

            }catch (Throwable e){
                //e.printStackTrace();
            }
            try{
                if (randomAccessFile!=null){
                    randomAccessFile.close();
                }
            }catch (Throwable e){

            }
        }
    }
    public static byte[] transform(Class clazz){
        System.out.println(clazz);
        return null;
    }
    public static void main(String[] args) {
        Map<String, Long> memoryLibraryMap = parseMemoryLibrary();
        if ("64".equals(System.getProperty("sun.arch.data.model"))){
            run64(memoryLibraryMap);
        }else {
            run32(memoryLibraryMap);
        }
        System.out.println();
    }
}
