(ns blog-manager.core
  (:gen-class)
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as string]))

;Text file format must be:
;Title
;Author
;DateDay-DateMonth-DateYear
;Body text (can be multiple lines)

(def textpath "./resources/text-posts/")
(def blogpath "./resources/blog-posts/")

(declare convert-and-write short-file-string-seq file-string-seq make-dirs text-posts-exist? name-only string-to-html map-to-html make-html-key split-metadata split-metadata-mem text-to-map text-to-map-mem get-date find-word)

(defn -main
  "With no arguments, convert text files into basic HTML documents. With an argument as a search term, display the text-posts that contain the argument"
  [& args]
  (make-dirs)
  (if (empty? args) ;If there are no arguments, attempt conversion from text to html. If there is an argument, search text files for the term
    (if-not (text-posts-exist?)
      (do
        (convert-and-write textpath blogpath)
        (println "Conversion completed."))
      (do
        (println (str "You have no text to turn into HTML. Add some text files to " textpath " and come back here to turn them into HTML pages in " blogpath))
        (println "Text files must be in this format:")
        (println "Title")
        (println "Author")
        (println "DateDay-DateMonth-DateYear (example: 26-2-2009")
        (println "Body text (can be multiple lines)"))
      )
    (let [search-term (first args)]
      (find-word textpath  search-term))
    )
  )

(defn convert-and-write
  "Take the text post from in-dir, convert it to html, and write the result to a file in out-dir"
  [in-dir out-dir]
  (doseq [f (file-string-seq in-dir)] (do (println "Converting " f) (spit (str out-dir (str (name-only (last (string/split f #"/"))) ".html")) (string-to-html (slurp f)))))
  )

(defn name-only
  "Get the name, without file extension, of filename"
  [filename]
  (first (string/split filename #"\.")))

(defn make-dirs
  "Creates directories for textpath and blogpath. Returns false if either cannot be created or already exists"
  []
  (.mkdirs (java.io.File. textpath))
  (.mkdirs (java.io.File. blogpath)))

(defn file-string-seq
  "Return a seq of strings of all file with relative path from program in directory at dir-path"
  [dir-path]
  (for [f (vec (drop 1 (file-seq (clojure.java.io/file dir-path))))] (str dir-path(.getName f))))

(defn text-posts-exist?
  "Returns true if there are text posts in textpath. Returns false otherwise"
  []
  (empty? (file-string-seq textpath)))


;Text-HTML transfer
(defn string-to-html
  "Take a textpost string and return an html blogpost"
  [text]
  (println "Making html...")
  ;Make a map based on line breaks in text
  (let [textmap (text-to-map-mem (nth (split-metadata-mem text) 0) (nth (split-metadata-mem text) 1) (nth (get-date (nth (split-metadata-mem text) 2)) 2) (nth (get-date (nth (split-metadata-mem text) 2)) 1) (nth (get-date (nth (split-metadata-mem text) 2)) 2) (nth (split-metadata-mem text) 3))]
    (println (str "Here is the text-map: " textmap))
    (str "<html>" (make-html-key :index textmap) (make-html-key :author textmap) (make-html-key :date textmap) (make-html-key :body textmap) "</html>")))

(defn map-to-html
  "Return a string with html code. Takes a text-to-map style map"
  ([text-map]
   (str (make-html-key :index text-map) (make-html-key :author text-map) (make-html-key :date text-map) (make-html-key :body text-map))))

(defn make-html-key
  "Returns a string based on the given index and its corresponding html code"
  [key text-map]
  (println (str "Making " key " html segment..."))
  (if (= key :index)
    (str (:index text-map) "<br>")
    (if (= key :author)
      (str "Author: <b>" (:index text-map) "</b><br>" 
           (if (= key :date)
             (str "Date: " (nth (:date text-map) 0) "-" (nth (:date text-map) 1) "-" (nth (:date text-map) 2) "<br>")))
      (if (= key :body)
        (str "<p>" (string/replace (:body text-map) #"\n" "</p><p>") "</p>")))))

(defn split-metadata
  "Splits text into metadata and body"
  [text-body]
  (println "Splitting to get metadata...")
  (vec (string/split text-body #"\n" 4)))

(defn get-date
  "Given a split text-body, make the date into a vector"
  [split-text]
  (println "Finding date")
  (vec (string/split split-text #"-")))

(defn text-to-map
  "Turns a divided text post into a map"
  [index author date-d date-m date-y body]
  (println "Making text map from html...")
  (hash-map :index index :author author :date [date-d date-m date-y] :body body ))

;Memoized functions
(def text-to-map-mem (memoize text-to-map))
(def split-metadata-mem (memoize split-metadata))

;Index and searching functions

(defn find-word
  "Print the files in dir which contain term"
  [dir term]
  (doseq [f (file-string-seq dir)]
    (if (.contains (slurp f) term)
      (println (str term " found in " f)))))



;Not needed, it seems
(defn add-unique
  "Returns a set with new conjoined"
  [new set]
  (set (conj set new)))

(defn split-all
  "Returns the seq of the given string split on \n and \" \" " 
  [string]
  (string/split (string/split string) #"\n") " ")
