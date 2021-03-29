class Queue {
  constructor(...elements) {
    // Initializing the queue with given arguments
    this.elements = [...elements];
  }
  // Proxying the push/shift methods
  push(...args) {
    return this.elements.push(...args);
  }
  shift(...args) {
    return this.elements.shift(...args);
  }
  // Add some length utility methods
  getLength(...args) {
    return this.elements.length;
  }
  setLength(length) {
    return this.elements.length = length;
  }
}
