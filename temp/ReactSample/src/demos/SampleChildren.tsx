import TextField from '@mui/material/TextField';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';

/** Reusable sample form fields to simulate real Flex children */
export function SampleChildren() {
  return (
    <>
      <Grid size={{ xs: 12, sm: 6 }}>
        <TextField fullWidth label="First Name" defaultValue="John" />
      </Grid>
      <Grid size={{ xs: 12, sm: 6 }}>
        <TextField fullWidth label="Last Name" defaultValue="Doe" />
      </Grid>
      <Grid size={{ xs: 12 }}>
        <TextField fullWidth label="Email" defaultValue="john@example.com" />
      </Grid>
      <Grid size={{ xs: 12 }}>
        <Typography variant="body2" color="text.secondary">
          These are sample children widgets inside the Flex container.
        </Typography>
      </Grid>
    </>
  );
}

export function SampleChildrenVertical() {
  return (
    <>
      <TextField fullWidth label="First Name" defaultValue="John" />
      <TextField fullWidth label="Last Name" defaultValue="Doe" />
      <TextField fullWidth label="Email" defaultValue="john@example.com" />
    </>
  );
}
