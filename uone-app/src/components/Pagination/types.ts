import React from 'react';

export interface IProps {
  disabled?: boolean;
  totalPages: number;
  onChanged: (event: React.ChangeEvent<unknown>, value: number) => void;
}

export default IProps;
