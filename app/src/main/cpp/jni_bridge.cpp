#include <jni.h>
#include <string>
#include <vector>
#include "native.h"

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_example_nativebridge_NativeBridge_nativeGetEngineVersion(
        JNIEnv* env,
        jobject /* this */) {
    std::string diagnostics = studyflow::DenseSlabAllocator::getInstance().getDiagnosticsString();
    std::string version = "StudyFlow Native Core v2.7 (16KB Page Aligned, AutoFDO Ready) - " + diagnostics;
    return env->NewStringUTF(version.c_str());
}

JNIEXPORT jobject JNICALL
Java_com_example_nativebridge_NativeBridge_nativeCategorize(
        JNIEnv* env,
        jobject /* this */,
        jstring jTitle,
        jstring jContent) {
    const char* titleChars = env->GetStringUTFChars(jTitle, nullptr);
    const char* contentChars = env->GetStringUTFChars(jContent, nullptr);

    std::string title = titleChars ? titleChars : "";
    std::string content = contentChars ? contentChars : "";

    if (titleChars) env->ReleaseStringUTFChars(jTitle, titleChars);
    if (contentChars) env->ReleaseStringUTFChars(jContent, contentChars);

    studyflow::CategorizationResult result = studyflow::categorizeContent(title, content);

    jclass resultClass = env->FindClass("com/example/nativebridge/CategorizeResult");
    if (!resultClass) return nullptr;

    jmethodID constructor = env->GetMethodID(
            resultClass,
            "<init>",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;D)V");
    if (!constructor) return nullptr;

    jstring jCourse = env->NewStringUTF(result.courseCode.c_str());
    jstring jTopic = env->NewStringUTF(result.topic.c_str());
    jstring jCat = env->NewStringUTF(result.category.c_str());

    jobject obj = env->NewObject(resultClass, constructor, jCourse, jTopic, jCat, result.confidence);
    return obj;
}

JNIEXPORT jobjectArray JNICALL
Java_com_example_nativebridge_NativeBridge_nativeGenerateFlashcards(
        JNIEnv* env,
        jobject /* this */,
        jstring jText,
        jint targetCount) {
    const char* textChars = env->GetStringUTFChars(jText, nullptr);
    std::string text = textChars ? textChars : "";
    if (textChars) env->ReleaseStringUTFChars(jText, textChars);

    std::vector<studyflow::FlashcardNative> cards = studyflow::generateFlashcards(text, targetCount);

    jclass cardClass = env->FindClass("com/example/nativebridge/FlashcardItem");
    if (!cardClass) return nullptr;

    jmethodID constructor = env->GetMethodID(
            cardClass,
            "<init>",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V");
    if (!constructor) return nullptr;

    jobjectArray array = env->NewObjectArray(cards.size(), cardClass, nullptr);
    for (size_t i = 0; i < cards.size(); ++i) {
        jstring q = env->NewStringUTF(cards[i].question.c_str());
        jstring a = env->NewStringUTF(cards[i].answer.c_str());
        jstring t = env->NewStringUTF(cards[i].topic.c_str());
        jobject cardObj = env->NewObject(cardClass, constructor, q, a, t, cards[i].difficulty);
        env->SetObjectArrayElement(array, i, cardObj);
        env->DeleteLocalRef(q);
        env->DeleteLocalRef(a);
        env->DeleteLocalRef(t);
        env->DeleteLocalRef(cardObj);
    }

    return array;
}

JNIEXPORT jdouble JNICALL
Java_com_example_nativebridge_NativeBridge_nativeCalculateMastery(
        JNIEnv* /* env */,
        jobject /* this */,
        jint totalCards,
        jint masteredCards,
        jint streak) {
    return studyflow::calculateMasteryScore(totalCards, masteredCards, streak);
}

JNIEXPORT void JNICALL
Java_com_example_nativebridge_NativeBridge_nativeIndexDocument(
        JNIEnv* env,
        jobject /* this */,
        jstring jId,
        jstring jTitle,
        jstring jText,
        jstring jModule) {
    const char* idChars = env->GetStringUTFChars(jId, nullptr);
    const char* titleChars = env->GetStringUTFChars(jTitle, nullptr);
    const char* textChars = env->GetStringUTFChars(jText, nullptr);
    const char* modChars = env->GetStringUTFChars(jModule, nullptr);

    studyflow::SemanticVectorEngine::getInstance().indexDocument(
        idChars ? idChars : "",
        titleChars ? titleChars : "",
        textChars ? textChars : "",
        modChars ? modChars : ""
    );

    if (idChars) env->ReleaseStringUTFChars(jId, idChars);
    if (titleChars) env->ReleaseStringUTFChars(jTitle, titleChars);
    if (textChars) env->ReleaseStringUTFChars(jText, textChars);
    if (modChars) env->ReleaseStringUTFChars(jModule, modChars);
}

JNIEXPORT void JNICALL
Java_com_example_nativebridge_NativeBridge_nativeClearVectorIndex(
        JNIEnv* /* env */,
        jobject /* this */) {
    studyflow::SemanticVectorEngine::getInstance().clear();
}

} // extern "C"
