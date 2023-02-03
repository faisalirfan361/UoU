const debounce = (fn: any, delay: number) => {
  let timer: any = null;
  return function (...args: any[]) {
    // @ts-ignore
    const context = this;
    timer && clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(context, args);
    }, delay);
  };
};

export default debounce;
