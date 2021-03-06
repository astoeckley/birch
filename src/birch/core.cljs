(ns birch.core
  (:require [clojure.string :as str]
            [cljs.tools.cli :refer [parse-opts]]))

(def node-fs (js/require "fs"))
(def node-path (js/require "path"))

(def read-dir (.-readdirSync node-fs))
(def stat (.-statSync node-fs))
(def path-join (.-join node-path))

(defn directory? [f]
  (.isDirectory (stat f)))

(def cli-color (js/require "cli-color"))
(def blue-text (.-blue cli-color))

(def I-branch "│   ")
(def T-branch "├── ")
(def L-branch "└── ")
(def SPACER   "    ")

(declare tree-entry)

(defn child-entries [path]
  (map #(tree-entry path %1) (read-dir path)))

(defn tree-entry [parent name]
  (let [path (path-join parent name)
        is-dir (directory? path)]
    {:name name
     :directory? is-dir
     :children (if is-dir (child-entries path))}))

(defn render-tree [color? {:keys [name children directory?]}]
  (cons
   (if (and color? directory?)
     (blue-text name)
     name)
   (mapcat (fn [child index]
             (let [subtree (render-tree color? child)
                   last? (= index (dec (count children)))
                   prefix-first (if last? L-branch T-branch)
                   prefix-rest  (if last? SPACER I-branch)]
               (cons (str prefix-first (first subtree))
                     (map #(str prefix-rest %) (next subtree)))))
           children
           (range))))

(def cli-opts [["-c" "--color" "Colorize the output"]])

(defn -main [& args]
  (let [{:keys [options arguments]} (parse-opts args cli-opts)]
    (->> (tree-entry "" (first arguments))
         (render-tree (:color options))
         (str/join "\n")
         print)))
