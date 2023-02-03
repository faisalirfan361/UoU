export interface Filter {
  name: string;
  value: string;
}

export interface GameFilterProps {
  options?: Filter[];
  defaultOption?: Filter;
  onSelect: (filter: Filter) => void;
}
