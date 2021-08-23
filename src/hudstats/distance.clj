(ns hudstats.distance
  (:require [geo.spatial :as spatial] )
  (:import java.text.DecimalFormat))

(def default-city [64.13548 -21.89541])
(def distance-format (DecimalFormat. "#"))

(def cities {"Reykjaví­k" [64.13548 -21.89541]
             "Akureyi" [65.68353 -18.0878]
             "Akranes" [64.32179 -22.0749]
             "Vestmannaeyjar" [63.44273 -20.27339]
             "Kopavogur" [64.11234 -21.91298]
             "Gardabaer" [64.08865 -21.92298]
             "Selfoss" [63.93311 -20.99712]
             "Saudarkrokur" [65.74611 -19.63944]
             "Grindaví­k" [63.83849 -22.43931]
             "Hafnarfjordur" [64.0671 -21.93774]
             "Mosfellsbaer" [64.16667 -21.7]
             "Seltjarnarnes" [64.15309 -21.99499]
             "Ísafjörður" [66.07475 -23.13498]
             "Keflavík" [63.9998 -22.5583]
             "Keflavík (Airport)" [63.985 -22.6282]
             "Hellissandur" [64.9163 -23.8757]
             "Egilsstaðir" [65.2669 -14.3948]
             "Höfn" [64.290665504 -15.222999108]
             "Hella" [63.835 -20.3919]
             "Húsaví­k" [65.4041 -13.6981]
             "Landmannalaugar" [63.9830 -19.0670]
             "Hvolsvöllur" [63.749997 -20.2333324]
             "Reykholt" [64.6657 -21.2871]
             "Mývatn" [65.6039 -16.9961]
             "Kirkjubæjarklaustur" [63.7833302 -18.0666664]
             "Vík" [63.4186 -19.0060]
             "Sandgerði" [64.051833126 -22.70249719]})

(defn distance [from to]
  (let [from-encoded (apply spatial/spatial4j-point (get cities from default-city))
        to-encoded (apply spatial/spatial4j-point (get cities to default-city))]
    (str (.format distance-format (/ (spatial/distance from-encoded to-encoded) 1000)) "km")))