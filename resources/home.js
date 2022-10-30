const dateContainer = document.querySelector(".dates");

document.getElementById("add-manual-dates").addEventListener("click", () => {
  const newDate = document.createElement("input");
  newDate.name = "date";
  newDate.type = "date";
  dateContainer.appendChild(newDate);
});
