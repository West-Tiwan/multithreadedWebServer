document.addEventListener("DOMContentLoaded", function() {
    const head = document.getElementsByTagName("h1")[0];
    if (head) {
        console.log(head);
        const para = document.createElement("p");
        para.textContent = "We hope you enjoyed the ride.";
        head.appendChild(para);
    } else {
        console.log("No h1 element found.");
    }
});