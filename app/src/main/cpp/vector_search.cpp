#include "vector_search.h"
#include <algorithm>
#include <cmath>
#include <sstream>

namespace studyflow {

SemanticVectorEngine& SemanticVectorEngine::getInstance() {
    static SemanticVectorEngine instance;
    return instance;
}

SemanticVectorEngine::SemanticVectorEngine() {
    // Initialized ready for indexing
}

void SemanticVectorEngine::clear() {
    mEntries.clear();
}

size_t SemanticVectorEngine::getIndexCount() const {
    return mEntries.size();
}

std::vector<int8_t> SemanticVectorEngine::computeQuantizedEmbedding(const std::string& text) {
    std::vector<int8_t> vec(EMBEDDING_DIM, 0);
    if (text.empty()) return vec;

    std::vector<float> accum(EMBEDDING_DIM, 0.0f);

    // Hash character n-grams and token roots into 32 dimensional projection
    std::istringstream iss(text);
    std::string token;
    size_t tokenCount = 0;

    while (iss >> token) {
        tokenCount++;
        for (char& c : token) c = static_cast<char>(std::tolower(static_cast<unsigned char>(c)));

        uint32_t hash = 5381;
        for (char c : token) {
            hash = ((hash << 5) + hash) + static_cast<uint32_t>(c);
        }

        int bucket = hash % EMBEDDING_DIM;
        float weight = 1.0f / (1.0f + 0.1f * tokenCount);
        accum[bucket] += (hash & 1) ? weight : -weight;

        // Also hash bigrams for sequence semantics
        if (token.length() >= 4) {
            uint32_t subHash = (token[0] * 31 + token[1]) * 31 + token[2];
            int subBucket = subHash % EMBEDDING_DIM;
            accum[subBucket] += 0.5f;
        }
    }

    // Normalize and quantize to int8 [-127, 127]
    float norm = 0.0f;
    for (float v : accum) norm += v * v;
    norm = std::sqrt(norm);
    if (norm < 0.0001f) norm = 1.0f;

    for (int i = 0; i < EMBEDDING_DIM; ++i) {
        float normalized = (accum[i] / norm) * 127.0f;
        normalized = std::clamp(normalized, -127.0f, 127.0f);
        vec[i] = static_cast<int8_t>(normalized);
    }

    return vec;
}

float SemanticVectorEngine::cosineSimilarity(const std::vector<int8_t>& a, const std::vector<int8_t>& b) {
    if (a.size() != b.size() || a.empty()) return 0.0f;

    int32_t dot = 0;
    int32_t normA = 0;
    int32_t normB = 0;

    for (size_t i = 0; i < a.size(); ++i) {
        dot += a[i] * b[i];
        normA += a[i] * a[i];
        normB += b[i] * b[i];
    }

    if (normA == 0 || normB == 0) return 0.0f;
    return static_cast<float>(dot) / (std::sqrt(static_cast<float>(normA)) * std::sqrt(static_cast<float>(normB)));
}

void SemanticVectorEngine::indexDocument(const std::string& id, const std::string& title, const std::string& text, const std::string& module) {
    Entry entry;
    entry.id = id;
    entry.title = title;
    entry.text = text;
    entry.module = module;
    entry.quantizedVec = computeQuantizedEmbedding(title + " " + text);
    mEntries.push_back(entry);
}

std::vector<VectorSearchResult> SemanticVectorEngine::search(const std::string& query, int topK) {
    std::vector<VectorSearchResult> results;
    if (query.empty() || mEntries.empty()) return results;

    std::vector<int8_t> queryVec = computeQuantizedEmbedding(query);

    for (const auto& entry : mEntries) {
        float sim = cosineSimilarity(queryVec, entry.quantizedVec);
        
        // Exact substring boost
        std::string lowerText = entry.text;
        std::string lowerQuery = query;
        for (char& c : lowerText) c = static_cast<char>(std::tolower(static_cast<unsigned char>(c)));
        for (char& c : lowerQuery) c = static_cast<char>(std::tolower(static_cast<unsigned char>(c)));
        
        if (lowerText.find(lowerQuery) != std::string::npos) {
            sim += 0.35f;
        }

        if (sim > 0.15f) {
            VectorSearchResult res;
            res.documentId = entry.id;
            res.sourceTitle = entry.title;
            res.snippet = entry.text.substr(0, std::min<size_t>(entry.text.size(), 140));
            res.moduleType = entry.module;
            res.score = sim;
            results.push_back(res);
        }
    }

    std::sort(results.begin(), results.end(), [](const VectorSearchResult& a, const VectorSearchResult& b) {
        return a.score > b.score;
    });

    if (results.size() > static_cast<size_t>(topK)) {
        results.resize(topK);
    }

    return results;
}

} // namespace studyflow
