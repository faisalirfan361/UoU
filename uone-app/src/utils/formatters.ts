export const formatDescription = (desc: string) => {
  const maxLength = 220;
  const result = desc.replaceAll("<p>", "").replaceAll("</p>", "");
  return result.length > maxLength
    ? result.substring(0, maxLength) + "..."
    : result;
};
