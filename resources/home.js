const manualDateContainer = document.querySelector(
  ".date-input-wrapper.manual .dates"
);

document.getElementById("add-manual-dates").addEventListener("click", () => {
  const newDate = document.createElement("input");
  newDate.name = "date";
  newDate.type = "date";
  manualDateContainer.appendChild(newDate);
});

const dateInputWrappers = document.querySelectorAll(".date-input-wrapper");

document.querySelector(".date-input-select").addEventListener("change", (e) => {
  const inputType = e.target.selectedOptions[0].value;

  dateInputWrappers.forEach((wrapper) => {
    const classes = wrapper.classList;
    if (classes.contains(inputType)) {
      classes.add("active");

      wrapper.querySelectorAll("input").forEach((input) => {
        input.removeAttribute("disabled");
      });
    } else {
      classes.remove("active");

      wrapper.querySelectorAll("input").forEach((input) => {
        input.setAttribute("disabled", "");
      });
    }
  });
});

const allWeekdaysInput = document.querySelector(".all-weekdays");
const weekdays = Array.from(document.querySelectorAll(".weekdays .weekday"));

if (setEquals(new Set(weekdays.map((w) => w.checked)), new Set([false]))) {
  weekdays[weekdays.length - 1].setCustomValidity(
    "Must select at least one day."
  );
}

weekdays.forEach((weekday) => {
  weekday.addEventListener("change", () => {
    const values = new Set(weekdays.map((w) => w.checked));

    if (setEquals(values, new Set([true]))) {
      allWeekdaysInput.checked = true;
      allWeekdaysInput.indeterminate = false;

      weekdays.forEach((weekday) => weekday.setCustomValidity(""));
    } else if (setEquals(values, new Set([false]))) {
      allWeekdaysInput.checked = false;
      allWeekdaysInput.indeterminate = false;

      weekday.setCustomValidity("Must select at least one day.");
    } else {
      allWeekdaysInput.checked = false;
      allWeekdaysInput.indeterminate = true;

      weekdays.forEach((weekday) => weekday.setCustomValidity(""));
    }
  });
});

allWeekdaysInput.addEventListener("change", () => {
  const checked = allWeekdaysInput.checked;

  weekdays.forEach((w) => {
    w.checked = checked;
  });
});

const linearStartDate = document.querySelector("#linear-start-date");
const linearEndDate = document.querySelector("#linear-end-date");

function validateLinearDates() {
  if (
    linearStartDate.value &&
    linearEndDate.value &&
    linearStartDate.value > linearEndDate.value
  ) {
    linearEndDate.setCustomValidity("End date can't be before start date");
  } else {
    linearEndDate.setCustomValidity("");
  }
}

linearStartDate.addEventListener("change", validateLinearDates);
linearEndDate.addEventListener("change", validateLinearDates);

function setEquals(s1, s2) {
  return isSuperset(s1, s2) && isSuperset(s2, s1);
}

function isSuperset(set, subset) {
  for (const elem of subset) {
    if (!set.has(elem)) {
      return false;
    }
  }
  return true;
}
