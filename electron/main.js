const fs = require('fs')
const path = require('path')
if (fs.existsSync(path.join(__dirname, "./mainprocess/main-opt.js"))) {
    const ScalaJS = require("./mainprocess/main-opt.js")
} else if (fs.existsSync(path.join(__dirname, "./mainprocess/main-fastopt.js"))) {
    const ScalaJS = require("./mainprocess/main-opt.js")
} else {
    console.log("compiled files not found")
}
