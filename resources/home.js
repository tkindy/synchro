const dateContainer = document.querySelector(".dates");

document.getElementById("add-dates").addEventListener("click", () => {
  const numExistingDates = dateContainer.querySelectorAll("input").length;

  const newDate = document.createElement("input");
  newDate.id = "date" + numExistingDates;
  newDate.name = newDate.id;
  newDate.type = "date";
  dateContainer.appendChild(newDate);
});
