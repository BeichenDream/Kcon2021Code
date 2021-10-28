#include <jni.h>
#include <jvmti.h>
#include <sys/mman.h>


JNIEXPORT jvmtiError JNICALL Java_Hack_patchVM(jvmtiEnv* jvmti, void** data_ptr) {

	jvmtiCapabilities jvmcap = { 0 };

	jvmti->functions->SetEventNotificationMode(
		jvmti,
		JVMTI_DISABLE,
		JVMTI_EVENT_CLASS_FILE_LOAD_HOOK,
		NULL /* all threads */);
	jvmti->GetCapabilities(&jvmcap);
	jvmcap.can_generate_all_class_hook_events = 0;
	jvmcap.can_retransform_classes = 0;
	jvmcap.can_retransform_any_class = 0;
	jvmti->AddCapabilities(&jvmcap);

	return JVMTI_ERROR_NULL_POINTER;
}



inline void jprintf(JNIEnv* env, char* line) {
	char system_class_name[] = { 'j','a','v','a','/','l','a','n','g','/','S','y','s','t','e','m',0 };
	char GetStaticFieldID_name[] = { 'o','u','t',0 };
	char GetStaticFieldID_sig[] = { 'L','j','a','v','a','/','i','o','/','P','r','i','n','t','S','t','r','e','a','m',';',0 };
	char GetMethodID_name[] = { 'p','r','i','n','t','l','n',0 };
	char GetMethodID_sig[] = { '(','L','j','a','v','a','/','l','a','n','g','/','S','t','r','i','n','g',';',')','V',0 };

	jclass cls = env->functions->FindClass(env, system_class_name);
	jfieldID fID = env->functions->GetStaticFieldID(env, cls, GetStaticFieldID_name, GetStaticFieldID_sig);
	jobject out = env->functions->GetStaticObjectField(env, cls, fID);
	jclass outCls = env->functions->GetObjectClass(env, out);
	jmethodID mID = env->functions->GetMethodID(env, outCls, GetMethodID_name, GetMethodID_sig);
	jstring str = env->functions->NewStringUTF(env, line);
	env->functions->CallVoidMethod(env, out, mID, str);
	env->functions->DeleteLocalRef(env, str);
	env->functions->DeleteLocalRef(env, cls);
	env->functions->DeleteLocalRef(env, outCls);
	env->functions->DeleteLocalRef(env, out);
}

JNIEXPORT jlong JNICALL Java_Hack_inject(JNIEnv* env)
{
	JavaVM* vm = { 0 };
	jvmtiEnv* jvmti = { 0 };
	jvmtiCapabilities jvmcap;
	jint class_count = 0;
	jclass* classes = { 0 };
	if (env->functions->GetJavaVM(env, &vm) == JNI_OK)
	{
		if (vm->functions->GetEnv(vm, (void**)&jvmti, JVMTI_VERSION_1_1)== JNI_OK)
		{

			if (jvmti->functions->GetCapabilities(jvmti, &jvmcap) == JVMTI_ERROR_NONE)
			{

				jvmcap.can_redefine_any_class = 1;
				jvmcap.can_redefine_classes = 1;

				if (jvmti->functions->AddCapabilities(jvmti, &jvmcap) == JVMTI_ERROR_NONE && jvmti->functions->GetLoadedClasses(jvmti, &class_count, &classes) == JVMTI_ERROR_NONE)
				{
					jclass transformGod = NULL;
					jmethodID transformMethod = NULL;

					for (size_t i = 0; i < class_count; i++)
					{
						char* signature_ptr = { 0 };
						char* generic_ptr = { 0 };
						jclass javaClass = classes[i];

						if (JVMTI_ERROR_NONE == jvmti->functions->GetClassSignature(jvmti, javaClass, &signature_ptr, &generic_ptr))
						{
							char* className = (char*)0x8aaaaa9a8aaaaa9a;//{ 'L','G','o','d','S','u','p','e','r','A',';',0 };
							int ret = 0;
							while (!(ret = *(unsigned char*)signature_ptr - *(unsigned char*)className) && *signature_ptr) {
								++signature_ptr, ++className;
							}

							if (ret == 0)
							{
								char methodName[] = { 't','r','a','n','s','f','o','r','m',0 };
								char methodSig[] = { '(','L','j','a','v','a','/','l','a','n','g','/','C','l','a','s','s',';',')','[','B',0 };
								jmethodID methodId = env->functions->GetStaticMethodID(env, javaClass, methodName, methodSig);
								if (methodId != NULL)
								{
									transformGod = javaClass;
									transformMethod = methodId;
									break;
								}
								else if (env->functions->ExceptionCheck(env))
								{
									env->functions->ExceptionClear(env);
								}
							}
						}

					}

					for (size_t i = 0; i < class_count; i++)
					{
						jclass javaClass = classes[i];
						if (transformGod != NULL && transformMethod != NULL)
						{
							jbyteArray  classBytes = (jbyteArray)env->functions->CallStaticObjectMethod(env, transformGod, transformMethod, javaClass);
							if (classBytes != NULL)
							{
								jboolean isCopy = { 0 };
								const unsigned char* bytes = (const unsigned char*)env->functions->GetByteArrayElements(env, classBytes, &isCopy);
								jsize size = env->functions->GetArrayLength(env, classBytes);
								jvmtiClassDefinition jcd = {};
								jcd.class_bytes = bytes;
								jcd.class_byte_count = size;
								jcd.klass = javaClass;
								jvmti->functions->RedefineClasses(jvmti, 1, &jcd);
								env->functions->ReleaseByteArrayElements(env, classBytes, (jbyte*)bytes, 0);
								env->functions->DeleteLocalRef(env, classBytes);
							}
							if (env->functions->ExceptionCheck(env))
							{
								env->functions->ExceptionClear(env);
							}
						}
						env->functions->DeleteLocalRef(env, javaClass);
					}
					if (transformGod != NULL && transformMethod!=NULL)
					{
						return 7788;
					}
				}
			}
		}
	}
	return  (jlong)0;
}

JNIEXPORT jlong JNICALL Java_Hack_JvmTi(JNIEnv* env)
{
	jsize count = 0;
	JavaVM* vm = { 0 };
	jvmtiEnv* jvmti = { 0 };
	jvmtiCapabilities jvmcap = { 0 };
	count = env->functions->GetJavaVM(env,&vm);
	if (count == JNI_OK)
	{
		if (vm->functions->GetEnv(vm, (void**)&jvmti, JVMTI_VERSION_1_1) == JNI_OK)
		{
			if (jvmti->functions->GetCapabilities(jvmti, &jvmcap) == JVMTI_ERROR_NONE)
			{
				jvmcap.can_redefine_any_class = 1;
				jvmcap.can_redefine_classes = 1;
				if (jvmti->functions->AddCapabilities(jvmti, &jvmcap)== JVMTI_ERROR_NONE)//添加权能
				{
					return (jlong)jvmti;
				} 
			}

		}
	}
	return (jlong)0;
}

JNIEXPORT jlong JNICALL Java_Hack_jmp(JNIEnv* env)
{
	void* fun = (void*)0xAAAAAAAAAAAAAAAA;
	return (jlong)((void* (*)(JNIEnv*))fun)(env);
}

JNIEXPORT jlong JNICALL Java_Hack_mmap()
{
	void* fun = (void*)0xAAAAAAAAAAAAAAAA;
	//void *mmap(void * addr , size_t length , int prot , int flags , int fd , off_t offset ); 
	void* address = ((void* (JNICALL*)(void*, size_t, int, int, int, off_t))fun)(0, 1024 * 1024 * 1, PROT_READ | PROT_WRITE | PROT_EXEC, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
	return (jlong)address;
}
