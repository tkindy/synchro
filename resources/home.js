const dateContainer = document.querySelector(".dates");

document.getElementById("add-dates").addEventListener("click", () => {
  const numExistingDates = dateContainer.querySelectorAll("input").length;

  const newDate = document.createElement("input");
  newDate.name = "date-" + numExistingDates;
  newDate.type = "date";
  dateContainer.appendChild(newDate);
});
