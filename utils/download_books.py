import gutenbergpy.textget

def download_book(id: int) -> (str, str):
    raw_book = gutenbergpy.textget.get_text_by_id(id)
    text = raw_book.decode('utf-8')
    title = text.split("\n")[0]
    title = title.replace("The Project Gutenberg EBook of ", "")
    title = title.replace("The Project Gutenberg eBook of ", "")
    title = title.replace("The Project Gutenberg eBook, ", "")
    title = title.strip()
    clean_book = gutenbergpy.textget.strip_headers(raw_book) 
    return (title, clean_book.decode('utf-8'))

def save_book(id: int):
    
    try:
        t, clean_book = download_book(id)
        with open("books/" + t, 'w') as file:
            file.write(clean_book)
        print(f"File '{t}' saved successfully.")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    for i in range(2701, 3000):
        save_book(i)