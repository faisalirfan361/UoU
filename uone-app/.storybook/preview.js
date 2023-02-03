import React from 'react';
import { muiTheme } from 'storybook-addon-material-ui';
import theme from '../src/theme';
import { MemoryRouter } from 'react-router';

const routerDecorator = (story) => (
  <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>
);

export const decorators = [
	muiTheme([theme]),
  routerDecorator
];
