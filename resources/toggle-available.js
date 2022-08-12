const sequence = ["unchecked", "checked", "ifneedbe"];

const element = event.target;

const classList = Array.from(element.classList);
const stateClass = classList[1];
const newStateClass = sequence[(sequence.indexOf(stateClass) + 1) % 3];

const newClassList = classList
  .slice(0, 1)
  .concat([newStateClass])
  .concat(classList.slice(2));

element.className = newClassList.join(" ");

const inputValueMap = {
  unchecked: "unavailable",
  checked: "available",
  ifneedbe: "ifneedbe",
};

element.parentElement.querySelector("input").value =
  inputValueMap[newStateClass];
