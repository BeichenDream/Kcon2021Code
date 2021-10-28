import sun.misc.Unsafe;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

public class JdkSecurityBypass{


    private static Unsafe getUnsafe() {
        Unsafe unsafe = null;

        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return unsafe;
    }
    private static Method getMethod(Class clazz,String methodName,Class[] params) {
        Method method = null;
        while (clazz!=null){
            try {
                method = clazz.getDeclaredMethod(methodName,params);
                break;
            }catch (NoSuchMethodException e){
                clazz = clazz.getSuperclass();
            }
        }
        return method;
    }
    public static byte[] readInputStream(InputStream inputStream) {
        byte[] temp = new byte[4096];
        int readOneNum = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            while ((readOneNum = inputStream.read(temp)) != -1) {
                bos.write(temp, 0, readOneNum);
            }
            inputStream.close();
        }catch (Exception e){
        }
        return bos.toByteArray();
    }



    public void bypassModule(){
        try {
            Unsafe unsafe = getUnsafe();
            Class InstrumentationImplClass = Class.forName("sun.instrument.InstrumentationImpl");
            Class currentClass = this.getClass();
            Constructor InstrumentationImplConstructor = null;
            try {
                InstrumentationImplConstructor = InstrumentationImplClass.getDeclaredConstructor(new Class[]{long.class,boolean.class,boolean.class});
                InstrumentationImplConstructor.setAccessible(true);
                Instrumentation instrumentationInstance= (Instrumentation) InstrumentationImplConstructor.newInstance(0,true,true);
                System.out.println(String.format("<JDK16 无需bypass instrumentationInstance:%s", instrumentationInstance));
            }catch (Exception e) {
                Method getModuleMethod = getMethod(Class.class, "getModule", new Class[0]);
                if (getModuleMethod != null) {
                    Object oldModule = getModuleMethod.invoke(currentClass, new Object[]{});
                    Object targetModule = getModuleMethod.invoke(InstrumentationImplClass, new Object[]{});
                    unsafe.getAndSetObject(currentClass, unsafe.objectFieldOffset(Class.class.getDeclaredField("module")), targetModule);
                    InstrumentationImplConstructor = InstrumentationImplClass.getDeclaredConstructor(new Class[]{long.class,boolean.class,boolean.class});
                    InstrumentationImplConstructor.setAccessible(true);
                    Instrumentation instrumentationInstance= (Instrumentation) InstrumentationImplConstructor.newInstance(0,true,true);
                    if (instrumentationInstance != null) {
                        System.out.println(String.format("Bypass Jdk Security Module instrumentationInstance:%s Successfully!", instrumentationInstance));
                        unsafe.getAndSetObject(currentClass, unsafe.objectFieldOffset(Class.class.getDeclaredField("module")), oldModule);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void removeClassCache(Unsafe unsafe,Class clazz){
        try {
            Class ClassAnonymousClass = unsafe.defineAnonymousClass(clazz,readInputStream(Class.class.getResourceAsStream("Class.class")),null);
            Field reflectionDataField = ClassAnonymousClass.getDeclaredField("reflectionData");
            unsafe.putObject(clazz,unsafe.objectFieldOffset(reflectionDataField),null);
        }catch (Exception e){
            //e.printStackTrace();
        }
    }

    public void bypassReflectionFilter(){
        try {
            Unsafe unsafe = getUnsafe();
            Class classClass = Class.class;
            try {

                System.out.println(String.format("没有Reflection Filter ClassLoader :%s", classClass.getDeclaredField("classLoader")));
            }catch (Exception e){
                try {
                    Class reflectionClass=Class.forName("jdk.internal.reflect.Reflection");
                    byte[] classBuffer = readInputStream(reflectionClass.getResourceAsStream("Reflection.class"));
                    Class reflectionAnonymousClass = unsafe.defineAnonymousClass(reflectionClass,classBuffer,null);

                    Field fieldFilterMapField=reflectionAnonymousClass.getDeclaredField("fieldFilterMap");
                    Field methodFilterMapField=reflectionAnonymousClass.getDeclaredField("methodFilterMap");

                    if(fieldFilterMapField.getType().isAssignableFrom(HashMap.class)){
                        unsafe.putObject(reflectionClass,unsafe.staticFieldOffset(fieldFilterMapField),new HashMap());
                    }
                    if(methodFilterMapField.getType().isAssignableFrom(HashMap.class)){
                        unsafe.putObject(reflectionClass,unsafe.staticFieldOffset(methodFilterMapField),new HashMap());
                    }
                    removeClassCache(unsafe,classClass);
                    System.out.println(String.format("Bypass Jdk Reflection Filter Successfully! ClassLoader :%s", classClass.getDeclaredField("classLoader")));

                }catch (ClassNotFoundException e2){
                    try {
                        Class reflectionClass=Class.forName("sun.reflect.Reflection");
                        byte[] classBuffer = readInputStream(reflectionClass.getResourceAsStream("Reflection.class"));
                        Class reflectionAnonymousClass = unsafe.defineAnonymousClass(reflectionClass,classBuffer,null);

                        Field fieldFilterMapField=reflectionAnonymousClass.getDeclaredField("fieldFilterMap");
                        Field methodFilterMapField=reflectionAnonymousClass.getDeclaredField("methodFilterMap");

                        if(fieldFilterMapField.getType().isAssignableFrom(HashMap.class)){
                            unsafe.putObject(reflectionClass,unsafe.staticFieldOffset(fieldFilterMapField),new HashMap());
                        }
                        if(methodFilterMapField.getType().isAssignableFrom(HashMap.class)){
                            unsafe.putObject(reflectionClass,unsafe.staticFieldOffset(methodFilterMapField),new HashMap());
                        }
                        removeClassCache(unsafe,classClass);
                        System.out.println(String.format("Bypass Jdk Reflection Filter Successfully! ClassLoader :%s", classClass.getDeclaredField("classLoader")));

                    }catch (Exception e3){

                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        //绕过Java 反射过滤获取ClassLoader私有字段
        new JdkSecurityBypass().bypassReflectionFilter();
        //绕过Jdk Module访问限制 可以访问任意类(即使是未声明导出) 任意私有方法
        new JdkSecurityBypass().bypassModule();

    }
}
