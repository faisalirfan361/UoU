export const required = () => `This field required.`;
export const min = (value) => `The min value is ${value?.toDateString?.() ?? value}`
export const max = (value) => `The max value is ${value?.toDateString?.() ?? value}`

export const possive = () => `The value should be a positive value`;
export const integer = () => `The value should be an integer value`;
