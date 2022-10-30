const manualDateContainer = document.querySelector(".manual-dates .dates");

document.getElementById("add-manual-dates").addEventListener("click", () => {
  const newDate = document.createElement("input");
  newDate.name = "date";
  newDate.type = "date";
  manualDateContainer.appendChild(newDate);
});
