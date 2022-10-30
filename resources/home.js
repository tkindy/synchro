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
    const classes = new Set(wrapper.classList);
    if (classes.has(inputType)) {
      classes.add("active");
    } else {
      classes.delete("active");
    }

    wrapper.className = Array.from(classes).join(" ");
  });
});
