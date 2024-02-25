const sequence = ["unchecked", "checked", "ifneedbe"];
const states = {
  unchecked: { inputValue: "unavailable", label: "Unavailable" },
  checked: { inputValue: "available", label: "Available" },
  ifneedbe: { inputValue: "ifneedbe", label: "If need be" },
};

document.querySelectorAll(".checkbox.active").forEach((element) => {
  element.addEventListener("click", (e) => {
    e.preventDefault();

    const classList = Array.from(element.classList);
    const stateClass = classList[1];
    const newStateClass = sequence[(sequence.indexOf(stateClass) + 1) % 3];

    const newClassList = classList
      .slice(0, 1)
      .concat([newStateClass])
      .concat(classList.slice(2));

    element.className = newClassList.join(" ");

    const { inputValue, label } = states[newStateClass];

    element.parentElement.querySelector("input").value = inputValue;
    element.ariaLabel = label;
  });
});
