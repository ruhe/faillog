(ns faillog.jenkins-test
  (:require [clojure.test :refer :all]
            [faillog.jenkins :refer :all]))

(deftest test-find-bugs
  (testing "Find bug ID in long description"
    (is (= 1234567
           (find-bug "https://bugs.launchpad.net/mos/+bug/1234567")))
    (is (= 1234567
           (find-bug "https://hostname/1234567 and
                      https://example.com/7654321"))))
  (testing "Return nil if there is no bug ID in description"
    (is (nil? (find-bug "Nothing to see. Move along")))
    (is (nil? (find-bug "https//launchpad.net/1234")))))

(def sample-build
  {:firstBuild {:number 1}
   :lastFailedBuild {:number 10}})

(deftest test-get-build-number-range
  (with-redefs-fn {#'faillog.jenkins/get-build-info
                   (fn [url job] sample-build)}
    #(is (= (range 1 11)
            (get-build-number-range "fake" "fake")))))
