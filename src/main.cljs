(ns main
  (:require [net]
    [clojure.string :refer [trim blank?]]))

(def table (atom {}))

(defn err-callback [err]
  (if (not= err nil) (println err)))

(defn create-record [data socket]
  { :remoteAddress (.-remoteAddress socket)
    :remotePort (.-remotePort socket)
    :id (nth data 0 nil)
    :localAddress (nth data 1 nil)
    :localPort (js/parseInt (nth data 2 nil) 10)
    })

(defn end-callback []
  (println "Connection closed."))

(defn send-table [socket]
  (.write socket (js/JSON.stringify (clj->js @table) nil 2)))

(defn data-callback [data socket]
    (let [str-data (.toString data)
          arr-data (.split (trim str-data) ",")
          rec-data (create-record arr-data socket)]
      (if (blank? (:id rec-data))
        (.write socket "Wrong data.\n")
        (do
          (swap! table assoc (:id rec-data) rec-data)
          (send-table socket)))))

(defn socket-handler [socket]
  (.on socket "data"  #(data-callback % socket))
  (.on socket "end"   end-callback)
  (.on socket "error" err-callback)
  (send-table socket))

(defn main! []
  (println "s world!")
  (let [server (.createServer net socket-handler)]
    (.listen server 9090 err-callback)))
