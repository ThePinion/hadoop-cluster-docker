import gutenbergpy.textget

def usage_example(id: int) -> str:
    raw_book = gutenbergpy.textget.get_text_by_id(2701)
    clean_book = gutenbergpy.textget.strip_headers(raw_book) 
    return clean_book

if __name__ == "__main__":
    print(usage_example(2701))
