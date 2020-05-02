{
  person: {
    name: "Chris",
    in_ca: true
  },
  price: {
    value: 10000
  },
  states: {
    ca: {
      taxed_value: function () {
        return this.price.value - (this.price.value * 0.4);
      }
    }
  }
}
