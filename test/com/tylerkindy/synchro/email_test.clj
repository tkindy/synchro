(ns com.tylerkindy.synchro.email-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.tylerkindy.synchro.email :refer [build-notification-email]]))

(deftest build-notification-email-single-entry
  (let [result (build-notification-email
                {:to "alice@example.com"
                 :description "Board Game Night"
                 :base-url "https://synchro.tylerkindy.com"
                 :plan-id "abc-123"
                 :respondent-count 3
                 :entries [{:person-name "Tyler" :action "submitted"}]})]
    (testing "subject"
      (is (= "New activity on 'Board Game Night'" (:subject result))))
    (testing "to"
      (is (= "alice@example.com" (:to result))))
    (testing "body"
      (is (= (str "<p>Tyler submitted their availability</p>"
                   "<p>3 people have responded so far.</p>"
                   "<p><a href=\"https://synchro.tylerkindy.com/plans/abc-123\">View the plan</a></p>")
             (:message result))))))

(deftest build-notification-email-multiple-entries
  (let [result (build-notification-email
                {:to "alice@example.com"
                 :description "Board Game Night"
                 :base-url "https://synchro.tylerkindy.com"
                 :plan-id "abc-123"
                 :respondent-count 5
                 :entries [{:person-name "Tyler" :action "submitted"}
                           {:person-name "Alex" :action "updated"}]})]
    (testing "subject is the same regardless of entry count"
      (is (= "New activity on 'Board Game Night'" (:subject result))))
    (testing "body lists each person on a separate line"
      (is (= (str "<p>Tyler submitted their availability</p>\n"
                   "<p>Alex updated their availability</p>"
                   "<p>5 people have responded so far.</p>"
                   "<p><a href=\"https://synchro.tylerkindy.com/plans/abc-123\">View the plan</a></p>")
             (:message result))))))

(deftest build-notification-email-single-respondent
  (let [result (build-notification-email
                {:to "alice@example.com"
                 :description "Board Game Night"
                 :base-url "https://synchro.tylerkindy.com"
                 :plan-id "abc-123"
                 :respondent-count 1
                 :entries [{:person-name "Tyler" :action "submitted"}]})]
    (testing "singular form for one respondent"
      (is (= (str "<p>Tyler submitted their availability</p>"
                   "<p>1 person has responded so far.</p>"
                   "<p><a href=\"https://synchro.tylerkindy.com/plans/abc-123\">View the plan</a></p>")
             (:message result))))))

(deftest build-notification-email-special-characters-in-description
  (let [result (build-notification-email
                {:to "bob@example.com"
                 :description "Tyler's Plan"
                 :base-url "https://synchro.tylerkindy.com"
                 :plan-id "def-456"
                 :respondent-count 2
                 :entries [{:person-name "Jo" :action "submitted"}]})]
    (testing "description with apostrophe renders correctly"
      (is (= "New activity on 'Tyler's Plan'" (:subject result))))))
