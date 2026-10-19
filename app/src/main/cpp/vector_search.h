#ifndef STUDYFLOW_VECTOR_SEARCH_H
#define STUDYFLOW_VECTOR_SEARCH_H

#include <string>
#include <vector>
#include <cstdint>

namespace studyflow {

struct VectorSearchResult {
    std::string documentId;
    std::string snippet;
    std::string sourceTitle;
    std::string moduleType; // "Notes", "Reviewer", "Tasks", "Schedule"
    float score;            // Cosine similarity
};

class SemanticVectorEngine {
public:
    static SemanticVectorEngine& getInstance();

    // Index a text snippet with its category/source
    void indexDocument(const std::string& id, const std::string& title, const std::string& text, const std::string& module);

    // Clear indexed entries
    void clear();

    // Query across indexed concepts with semantic expansion
    std::vector<VectorSearchResult> search(const std::string& query, int topK = 6);

    size_t getIndexCount() const;

private:
    SemanticVectorEngine();

    // Lightweight 32-dimensional quantized embedding projection
    static constexpr int EMBEDDING_DIM = 32;

    struct Entry {
        std::string id;
        std::string title;
        std::string text;
        std::string module;
        std::vector<int8_t> quantizedVec;
    };

    std::vector<Entry> mEntries;

    std::vector<int8_t> computeQuantizedEmbedding(const std::string& text);
    float cosineSimilarity(const std::vector<int8_t>& a, const std::vector<int8_t>& b);
};

} // namespace studyflow

#endif // STUDYFLOW_VECTOR_SEARCH_H
