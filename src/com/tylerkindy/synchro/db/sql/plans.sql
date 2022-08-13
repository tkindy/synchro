-- :name insert-plan :! :n
INSERT INTO plans (description)
VALUES (:description);

-- :name insert-dates :! :n
INSERT INTO plan_dates (plan_id, date)
VALUES :tuple*:dates;
