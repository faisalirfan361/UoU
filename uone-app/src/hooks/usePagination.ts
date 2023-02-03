import { useMemo } from "react";
import { useState } from "react";

export default function usePagination<T>(data: T[], itemsPerPage: number) {
  const [currentPage, setCurrentPage] = useState(1);
  const maxPage = Math.ceil(data.length / itemsPerPage);

  const currentData = useMemo(() => {
    const begin = (currentPage - 1) * itemsPerPage;
    const end = begin + itemsPerPage;
    const result = data.slice(begin, end);
    if (result.length < itemsPerPage) {
      const emptySlots = new Array(itemsPerPage - result.length).fill(null);
      return [...result, ...emptySlots];
    }
    return result;
  }, [currentPage, data]);

  const next = () => {
    setCurrentPage((currentPage) => Math.min(currentPage + 1, maxPage));
  };

  const prev = () => {
    setCurrentPage((currentPage) => Math.max(currentPage - 1, 1));
  };

  const jump = (page: number) => {
    const pageNumber = Math.max(1, page);
    setCurrentPage(Math.min(pageNumber, maxPage));
  };

  return { next, prev, jump, data: currentData, currentPage, maxPage };
}
