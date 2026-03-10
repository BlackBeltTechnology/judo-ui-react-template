/**
 * BASELINE: Plain Flex (no card, no collapsible)
 * Maps to: flex.hbs else branch — <Grid container>
 */
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Box from '@mui/material/Box';
import { SampleChildrenVertical } from './SampleChildren';

export function PlainFlex() {
  return (
    <Box>
      <Typography variant="h5" gutterBottom>Baseline: Plain Flex (vertical)</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        This is how a Flex renders today with no card flag. Uses Grid + Stack.
      </Typography>

      {/* Mimics flex.hbs else branch */}
      <Grid data-testid="plain-flex" size={{ xs: 12 }}>
        <Grid container direction="row" spacing={2}>
          <Grid size={{ xs: 12 }}>
            <Stack spacing={2} className="force-full-width">
              <SampleChildrenVertical />
            </Stack>
          </Grid>
        </Grid>
      </Grid>
    </Box>
  );
}
